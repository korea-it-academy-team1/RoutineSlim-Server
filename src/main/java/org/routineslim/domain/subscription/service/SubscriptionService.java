package org.routineslim.domain.subscription.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.routineslim.domain.subscription.dto.SubscriptionCreateRequest;
import org.routineslim.domain.subscription.dto.SubscriptionResponse;
import org.routineslim.domain.subscription.dto.SubscriptionStatusRequest;
import org.routineslim.domain.subscription.dto.SubscriptionUpdateRequest;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.subscription.repository.SubscriptionRepository;
import org.routineslim.global.util.UuidUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final String DEFAULT_CURRENCY = "KRW";
    private static final int DEFAULT_CYCLE_INTERVAL = 1;

    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    /** 내 구독 목록. 다음 결제일이 가까운 순. status 를 주면 그 상태만 */
    public List<SubscriptionResponse> getSubscriptions(UUID memberId, SubscriptionStatus status) {
        List<Subscription> subscriptions = status == null
                ? subscriptionRepository.findByMemberIdAndDeletedAtIsNullOrderByNextBillingDateAscNameAsc(memberId)
                : subscriptionRepository.findByMemberIdAndStatusAndDeletedAtIsNullOrderByNextBillingDateAscNameAsc(
                        memberId, status);
        return subscriptions.stream().map(SubscriptionResponse::from).toList();
    }

    public SubscriptionResponse getSubscription(UUID memberId, UUID subscriptionId) {
        return SubscriptionResponse.from(findOwned(memberId, subscriptionId));
    }

    @Transactional
    public SubscriptionResponse create(UUID memberId, SubscriptionCreateRequest request) {
        if (subscriptionRepository.countActiveMember(UuidUtils.toBytes(memberId)) == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없어요.");
        }

        SubscriptionStatus status = request.status() == null ? SubscriptionStatus.ACTIVE : request.status();
        if (status == SubscriptionStatus.CANCELED) {
            throw badRequest("해지 상태로는 등록할 수 없어요.");
        }

        Subscription.Details details = new Subscription.Details(
                request.name().strip(),
                request.categoryId(),
                request.presetId(),
                request.cardId(),
                request.amount(),
                request.currency() == null ? DEFAULT_CURRENCY : request.currency(),
                request.billingCycle(),
                request.cycleInterval() == null ? DEFAULT_CYCLE_INTERVAL : request.cycleInterval(),
                request.startDate(),
                request.trialEndDate(),
                request.notifyEnabled() == null || request.notifyEnabled(),
                request.notifyDaysBefore(),
                blankToNull(request.memo())
        );
        validateDetails(memberId, details, status);

        UUID subscriptionId = request.id() == null ? UuidUtils.generateV7() : request.id();
        if (request.id() != null && subscriptionRepository.existsById(subscriptionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 구독 ID예요.");
        }

        Subscription subscription = Subscription.create(subscriptionId, memberId, details, status, today(), now());
        subscriptionRepository.save(subscription);
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse update(UUID memberId, UUID subscriptionId, SubscriptionUpdateRequest request) {
        Subscription subscription = findOwned(memberId, subscriptionId);

        Subscription.Details details = new Subscription.Details(
                request.name().strip(),
                request.categoryId(),
                request.presetId(),
                request.cardId(),
                request.amount(),
                request.currency() == null ? DEFAULT_CURRENCY : request.currency(),
                request.billingCycle(),
                request.cycleInterval() == null ? DEFAULT_CYCLE_INTERVAL : request.cycleInterval(),
                request.startDate(),
                request.trialEndDate(),
                request.notifyEnabled() == null || request.notifyEnabled(),
                request.notifyDaysBefore(),
                blankToNull(request.memo())
        );
        validateDetails(memberId, details, subscription.getStatus());

        subscription.update(details, today(), now());
        return SubscriptionResponse.from(subscription);
    }

    /** 해지(CANCELED), 일시 정지(PAUSED), 재개(ACTIVE), 무료 체험(TRIAL) */
    @Transactional
    public SubscriptionResponse changeStatus(UUID memberId, UUID subscriptionId, SubscriptionStatusRequest request) {
        Subscription subscription = findOwned(memberId, subscriptionId);

        if (request.status() == SubscriptionStatus.TRIAL
                && request.trialEndDate() == null && subscription.getTrialEndDate() == null) {
            throw badRequest("무료 체험으로 바꾸려면 체험 종료일(trialEndDate)이 필요해요.");
        }

        subscription.changeStatus(request.status(), request.trialEndDate(), today(), now());
        return SubscriptionResponse.from(subscription);
    }

    /** 잘못 등록한 구독을 목록에서 지운다 (soft delete). 해지와는 다르다 */
    @Transactional
    public void delete(UUID memberId, UUID subscriptionId) {
        findOwned(memberId, subscriptionId).delete(now());
    }

    private Subscription findOwned(UUID memberId, UUID subscriptionId) {
        return subscriptionRepository.findByIdAndMemberIdAndDeletedAtIsNull(subscriptionId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "구독을 찾을 수 없어요."));
    }

    private void validateDetails(UUID memberId, Subscription.Details details, SubscriptionStatus status) {
        if (subscriptionRepository.countCategory(details.categoryId()) == 0) {
            throw badRequest("존재하지 않는 카테고리예요.");
        }
        if (details.presetId() != null && subscriptionRepository.countPreset(details.presetId()) == 0) {
            throw badRequest("존재하지 않는 프리셋이에요.");
        }
        if (details.cardId() != null
                && subscriptionRepository.countCardOfMember(details.cardId(), UuidUtils.toBytes(memberId)) == 0) {
            throw badRequest("등록하지 않은 카드예요.");
        }
        if (status == SubscriptionStatus.TRIAL && details.trialEndDate() == null) {
            throw badRequest("무료 체험 구독은 체험 종료일(trialEndDate)이 필요해요.");
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }
}
