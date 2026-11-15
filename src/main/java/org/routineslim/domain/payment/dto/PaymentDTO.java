package org.routineslim.domain.payment.dto;

import lombok.*;
import org.routineslim.domain.subscription.entity.BillingCycle;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    // 구독 식별자
    private UUID subscriptionId;

    // 구독 이름 (청구 기록에는 이름이 없어서 구독에서 가져온다)
    private String subscriptionName;

    // 카테고리 번호 (1 OTT, 2 음악, 3 클라우드, 4 쇼핑)
    private Long categoryId;

    // 청구일
    private LocalDate billedOn;

    // 청구 금액 (청구 당시 금액, 이후 요금이 바뀌어도 그대로)
    private Integer amount;

    // 통화 (KRW 등)
    private String currency;

    // 구독의 결제 주기 (WEEKLY, MONTHLY, YEARLY) — 캘린더 목록에 '매월' 등으로 표시
    private BillingCycle billingCycle;

    // 구독의 현재 상태 (TRIAL, ACTIVE, PAUSED, CANCELED) — 지금은 해지한 구독인지 표시
    private SubscriptionStatus status;
}
