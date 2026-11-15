package org.routineslim.domain.payment.service;

import org.routineslim.domain.payment.dto.MonthlyPaymentResponseDTO;
import org.routineslim.domain.payment.dto.PaymentDTO;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    // 월별 청구 기록 조회
    MonthlyPaymentResponseDTO getMonthlyPayments(UUID memberId, String yearMonth);

    // 한 구독의 최근 청구 기록 조회
    List<PaymentDTO> getSubscriptionPayments(UUID memberId, UUID subscriptionId, Integer limit);

}
