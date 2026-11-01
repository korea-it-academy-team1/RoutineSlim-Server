package org.routineslim.domain.subscription.dto;

import java.time.LocalDate;

import org.routineslim.domain.subscription.entity.BillingCycle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 구독 수정 요청 (PUT, 전체 교체).
 * 수정 화면은 모든 값을 다시 보내므로, 비운 값(null)은 '지운다'는 뜻이다.
 * 상태 변경(해지·정지·재개)은 PATCH /{id}/status 로 따로 한다.
 */
public record SubscriptionUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long categoryId,
        Long presetId,
        Long cardId,
        @NotNull @PositiveOrZero Integer amount,
        @Pattern(regexp = "[A-Z]{3}", message = "통화는 ISO 4217 세 글자 대문자(KRW, USD)로 보내 주세요") String currency,
        @NotNull BillingCycle billingCycle,
        @Min(1) @Max(99) Integer cycleInterval,
        @NotNull LocalDate startDate,
        LocalDate trialEndDate,
        Boolean notifyEnabled,
        @Min(0) @Max(30) Integer notifyDaysBefore,
        @Size(max = 1000) String memo
) {
}
