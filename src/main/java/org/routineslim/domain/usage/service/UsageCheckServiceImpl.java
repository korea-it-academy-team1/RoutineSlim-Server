package org.routineslim.domain.usage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.subscription.repository.SubscriptionRepository;
import org.routineslim.domain.usage.dto.UsageCheckDTO;
import org.routineslim.domain.usage.dto.UsageCheckRequestDTO;
import org.routineslim.domain.usage.entity.UsageCheck;
import org.routineslim.domain.usage.entity.UsageLevel;
import org.routineslim.domain.usage.repository.UsageCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class UsageCheckServiceImpl implements UsageCheckService {

    private final UsageCheckRepository usageCheckRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    // 회원의 사용량 체크 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<UsageCheckDTO> getUsageChecks(UUID memberId) {

        // 회원의 체크를 DTO 로 변환
        return usageCheckRepository.findByMemberId(memberId).stream()
                .map(this::toDTO)
                .toList();
    }

    // 구독의 사용량 체크 저장 (있으면 수정)
    @Override
    public UsageCheckDTO saveUsageCheck(UUID memberId, UUID subscriptionId, UsageCheckRequestDTO usageCheckRequestDTO) {

        // 사용 정도 확인
        String level = usageCheckRequestDTO.getUsageLevel();
        if (level == null || level.isBlank()) {
            throw new IllegalArgumentException("사용 정도(usageLevel)는 필수입니다.");
        }

        UsageLevel usageLevel;
        try {
            usageLevel = UsageLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("사용 정도는 OFTEN, SOMETIMES, RARELY, NEVER 중 하나여야 합니다.");
        }

        // 내 구독인지 확인 (삭제한 구독 제외)
        subscriptionRepository.findByIdAndMemberIdAndDeletedAtIsNull(subscriptionId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        // 기존 체크 조회
        UsageCheck usageCheck = usageCheckRepository.findBySubscriptionId(subscriptionId)
                .orElse(null);

        if (usageCheck != null) {
            // 기존 체크 수정
            usageCheck.setUsageLevel(usageLevel);
            usageCheck.setCheckedOn(LocalDate.now(clock));
        } else {
            // 새로운 체크 등록
            usageCheck = UsageCheck.builder()
                    .subscriptionId(subscriptionId)
                    .memberId(memberId)
                    .usageLevel(usageLevel)
                    .checkedOn(LocalDate.now(clock))
                    .build();
        }

        // DB 저장
        return toDTO(usageCheckRepository.save(usageCheck));
    }

    // 엔티티 → DTO
    private UsageCheckDTO toDTO(UsageCheck usageCheck) {
        return UsageCheckDTO.builder()
                .subscriptionId(usageCheck.getSubscriptionId())
                .usageLevel(usageCheck.getUsageLevel())
                .checkedOn(usageCheck.getCheckedOn())
                .build();
    }
}
