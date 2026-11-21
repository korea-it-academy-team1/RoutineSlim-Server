package org.routineslim.domain.cleanup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.cleanup.dto.CleanupItemDTO;
import org.routineslim.domain.cleanup.dto.CleanupSaveRequestDTO;
import org.routineslim.domain.cleanup.entity.CleanupAction;
import org.routineslim.domain.cleanup.entity.CleanupItem;
import org.routineslim.domain.cleanup.repository.CleanupItemRepository;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CleanupServiceImpl implements CleanupService {

    // 결제일 며칠 전에 알릴지
    private static final int REMIND_DAYS_BEFORE = 3;

    // '그대로 두기' (저장하지 않고 목록에서 뺀다)
    private static final String KEEP = "KEEP";

    private final CleanupItemRepository cleanupItemRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    // 정리 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<CleanupItemDTO> getCleanupItems(UUID memberId) {

        List<CleanupItem> items = cleanupItemRepository.findByMemberIdOrderByCreatedAtAscCleanupItemIdAsc(memberId);
        if (items.isEmpty()) {
            return List.of();
        }

        // 구독을 한 번에 조회
        Map<UUID, Subscription> subscriptions = subscriptionRepository
                .findAllById(items.stream().map(CleanupItem::getSubscriptionId).toList())
                .stream()
                .collect(Collectors.toMap(Subscription::getId, Function.identity()));

        // 이미 해지했거나 삭제한 구독은 정리가 끝난 것이라 보여 주지 않는다
        return items.stream()
                .filter(item -> isInUse(subscriptions.get(item.getSubscriptionId())))
                .map(item -> toDTO(item, subscriptions.get(item.getSubscriptionId())))
                .toList();
    }

    // 정리 목록에 담기
    @Override
    public List<CleanupItemDTO> saveCleanupItems(UUID memberId, CleanupSaveRequestDTO request) {

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("담을 구독이 없습니다.");
        }

        // 같은 구독이 두 번 오면 마지막 값만 쓴다
        Map<UUID, CleanupSaveRequestDTO.Item> bySubscription = new LinkedHashMap<>();
        for (CleanupSaveRequestDTO.Item item : request.getItems()) {
            if (item == null || item.getSubscriptionId() == null) {
                throw new IllegalArgumentException("구독 아이디(subscriptionId)는 필수입니다.");
            }
            bySubscription.put(item.getSubscriptionId(), item);
        }

        for (CleanupSaveRequestDTO.Item item : bySubscription.values()) {
            saveItem(memberId, item);
        }

        return getCleanupItems(memberId);
    }

    // 정리 목록에서 빼기
    @Override
    public void deleteCleanupItem(UUID memberId, UUID subscriptionId) {

        CleanupItem item = cleanupItemRepository.findBySubscriptionId(subscriptionId)
                .filter(found -> found.getMemberId().equals(memberId))
                .orElseThrow(() -> new IllegalArgumentException("정리 목록에 없는 구독입니다."));

        cleanupItemRepository.delete(item);
    }

    // ------------------------------------------------------------------------

    // 구독 1건 담기 (있으면 덮어쓰기, KEEP 이면 빼기)
    private void saveItem(UUID memberId, CleanupSaveRequestDTO.Item request) {

        // 내 구독인지 확인 (삭제한 구독 제외)
        Subscription subscription = subscriptionRepository
                .findByIdAndMemberIdAndDeletedAtIsNull(request.getSubscriptionId(), memberId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        Optional<CleanupItem> existing = cleanupItemRepository.findBySubscriptionId(subscription.getId());

        // 그대로 두기 → 목록에서 뺀다
        String actionText = request.getAction() == null ? "" : request.getAction().trim();
        if (KEEP.equals(actionText)) {
            existing.ifPresent(cleanupItemRepository::delete);
            return;
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new IllegalArgumentException("이미 해지한 구독입니다.");
        }

        CleanupAction action = parseAction(actionText);
        int monthlyAmount = subscription.getMonthlyAmount();
        int newMonthlyAmount = newMonthlyAmount(action, request.getNewMonthlyAmount(), monthlyAmount);

        CleanupItem item = existing.orElseGet(() -> CleanupItem.builder()
                .subscriptionId(subscription.getId())
                .memberId(memberId)
                .createdAt(LocalDateTime.now(clock).withNano(0))
                .build());

        item.setAction(action);
        item.setMonthlyAmount(monthlyAmount);
        item.setNewMonthlyAmount(newMonthlyAmount);
        item.setRemindOn(subscription.getNextBillingDate() == null
                ? null
                : subscription.getNextBillingDate().minusDays(REMIND_DAYS_BEFORE));

        cleanupItemRepository.save(item);
    }

    // 정리 후 월 금액 확인 (해지는 0, 나머지는 0 이상 지금 금액 이하)
    private int newMonthlyAmount(CleanupAction action, Integer requested, int monthlyAmount) {
        if (action == CleanupAction.CANCEL) {
            return 0;
        }
        if (requested == null) {
            throw new IllegalArgumentException("요금제 변경·파티원 모집은 정리 후 월 금액(newMonthlyAmount)이 필요합니다.");
        }
        if (requested < 0 || requested > monthlyAmount) {
            throw new IllegalArgumentException("정리 후 월 금액은 0원 이상, 지금 월 금액 이하여야 합니다.");
        }
        return requested;
    }

    private CleanupAction parseAction(String action) {
        if (action.isBlank()) {
            throw new IllegalArgumentException("정리 방법(action)은 필수입니다.");
        }
        try {
            return CleanupAction.valueOf(action);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("정리 방법은 CANCEL, DOWNGRADE, PARTY, KEEP 중 하나여야 합니다.");
        }
    }

    // 정리 목록에 보여 줄 구독인지 (있고, 삭제·해지 전)
    private boolean isInUse(Subscription subscription) {
        return subscription != null
                && subscription.getDeletedAt() == null
                && subscription.getStatus() != SubscriptionStatus.CANCELED;
    }

    // 엔티티 → DTO
    private CleanupItemDTO toDTO(CleanupItem item, Subscription subscription) {
        return CleanupItemDTO.builder()
                .subscriptionId(item.getSubscriptionId())
                .subscriptionName(subscription.getName())
                .action(item.getAction())
                .monthlyAmount(item.getMonthlyAmount())
                .newMonthlyAmount(item.getNewMonthlyAmount())
                .nextBillingDate(subscription.getNextBillingDate())
                .remindOn(item.getRemindOn())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
