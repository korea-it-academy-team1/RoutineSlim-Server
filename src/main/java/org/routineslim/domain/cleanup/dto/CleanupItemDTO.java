package org.routineslim.domain.cleanup.dto;

import lombok.*;
import org.routineslim.domain.cleanup.entity.CleanupAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// 정리 목록 1건
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleanupItemDTO {

    // 구독 아이디
    private UUID subscriptionId;

    // 구독 이름 (목록 표시용)
    private String subscriptionName;

    // 정리 방법 (CANCEL, DOWNGRADE, PARTY)
    private CleanupAction action;

    // 담을 때의 월 환산 금액
    private Integer monthlyAmount;

    // 정리 후 월 금액 (해지는 0)
    private Integer newMonthlyAmount;

    // 다음 결제일 (지금 구독 기준)
    private LocalDate nextBillingDate;

    // 알림 날짜 (담을 때의 다음 결제일 3일 전)
    private LocalDate remindOn;

    // 담은 일시
    private LocalDateTime createdAt;
}
