package org.routineslim.domain.subscription.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.routineslim.domain.subscription.entity.BillingCycle;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;

/**
 * 구독 응답.
 * monthlyAmount: 1회 금액을 월 단위로 환산한 값 (연간 결제 ÷ 12 등). 홈의 월 고정비 합계에 쓴다.
 */
public record SubscriptionResponse(
        UUID id,
        String name,
        Long categoryId,
        Long presetId,
        Long cardId,
        int amount,
        String currency,
        BillingCycle billingCycle,
        int cycleInterval,
        int monthlyAmount,
        LocalDate startDate,
        int billingDay,
        LocalDate nextBillingDate,
        SubscriptionStatus status,
        LocalDate trialEndDate,
        boolean notifyEnabled,
        Integer notifyDaysBefore,
        String memo,
        LocalDateTime canceledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getName(),
                s.getCategoryId(),
                s.getPresetId(),
                s.getCardId(),
                s.getAmount(),
                s.getCurrency(),
                s.getBillingCycle(),
                s.getCycleInterval(),
                s.getMonthlyAmount(),
                s.getStartDate(),
                s.getBillingDay(),
                s.getNextBillingDate(),
                s.getStatus(),
                s.getTrialEndDate(),
                s.getNotifyEnabled(),
                s.getNotifyDaysBefore(),
                s.getMemo(),
                s.getCanceledAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
