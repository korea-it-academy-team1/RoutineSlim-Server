package org.routineslim.domain.subscription.dto;

import java.time.LocalDate;

import org.routineslim.domain.subscription.entity.SubscriptionStatus;

import jakarta.validation.constraints.NotNull;

/**
 * 구독 상태 변경 요청.
 * CANCELED = 해지, PAUSED = 일시 정지, ACTIVE = 재개, TRIAL = 무료 체험 (trialEndDate 필요)
 */
public record SubscriptionStatusRequest(
        @NotNull SubscriptionStatus status,
        LocalDate trialEndDate
) {
}
