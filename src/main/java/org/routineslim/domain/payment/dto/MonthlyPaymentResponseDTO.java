package org.routineslim.domain.payment.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPaymentResponseDTO {

    // 조회한 달 (예: 2026-10)
    private String yearMonth;

    // 그 달 청구 금액 합계
    private Integer totalAmount;

    // 그 달 청구 건수
    private Integer count;

    // 청구 기록 목록 (청구일 순)
    private List<PaymentDTO> payments;
}
