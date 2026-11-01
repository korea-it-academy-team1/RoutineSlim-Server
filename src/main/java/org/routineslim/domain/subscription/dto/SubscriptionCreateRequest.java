package org.routineslim.domain.subscription.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.routineslim.domain.subscription.entity.BillingCycle;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 구독 등록 요청.
 * id 는 앱이 오프라인에서 UUIDv7로 만들어 보낼 때만 넣는다. 비우면 서버가 만든다.
 * status 는 TRIAL / ACTIVE / PAUSED 중 하나 (비우면 ACTIVE).
 */
public record SubscriptionCreateRequest(
        UUID id,
        @NotBlank @Size(max = 100) String name,
        @NotNull Long categoryId,
        Long presetId,
        Long cardId,
        @NotNull @PositiveOrZero Integer amount,
        @Pattern(regexp = "[A-Z]{3}", message = "통화는 ISO 4217 세 글자 대문자(KRW, USD)로 보내 주세요") String currency,
        @NotNull BillingCycle billingCycle,
        @Min(1) @Max(99) Integer cycleInterval,
        @NotNull LocalDate startDate,
        SubscriptionStatus status,
        LocalDate trialEndDate,
        Boolean notifyEnabled,
        @Min(0) @Max(30) Integer notifyDaysBefore,
        @Size(max = 1000) String memo
) {
}
