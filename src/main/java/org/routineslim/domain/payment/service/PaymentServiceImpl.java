package org.routineslim.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.payment.dto.MonthlyPaymentResponseDTO;
import org.routineslim.domain.payment.dto.PaymentDTO;
import org.routineslim.domain.subscription.repository.PaymentRepository;
import org.routineslim.domain.subscription.repository.SubscriptionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    // 월별 청구 기록 조회
    @Override
    public MonthlyPaymentResponseDTO getMonthlyPayments(UUID memberId, String yearMonth) {

        // 조회할 달 확인 (형식: yyyy-MM)
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new IllegalArgumentException("조회할 달(yearMonth)은 필수입니다.");
        }

        YearMonth month;
        try {
            month = YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("조회할 달은 yyyy-MM 형식이어야 합니다. (예: 2026-10)");
        }

        // 그 달 1일 ~ 말일의 청구 기록 조회 (삭제한 구독의 기록은 제외)
        List<PaymentDTO> payments = paymentRepository.findMonthlyPayments(
                memberId,
                month.atDay(1),
                month.atEndOfMonth()
        );

        // 청구 금액 합계
        int totalAmount = payments.stream()
                .mapToInt(PaymentDTO::getAmount)
                .sum();

        return MonthlyPaymentResponseDTO.builder()
                .yearMonth(month.toString())
                .totalAmount(totalAmount)
                .count(payments.size())
                .payments(payments)
                .build();
    }

    // 한 구독의 최근 청구 기록 조회
    @Override
    public List<PaymentDTO> getSubscriptionPayments(UUID memberId, UUID subscriptionId, Integer limit) {

        // 조회 개수 확인 (기본 6건, 최대 24건)
        int size = limit == null ? 6 : limit;
        if (size < 1 || size > 24) {
            throw new IllegalArgumentException("조회 개수(limit)는 1~24 사이여야 합니다.");
        }

        // 내 구독인지 확인 (삭제한 구독 제외)
        subscriptionRepository.findByIdAndMemberIdAndDeletedAtIsNull(subscriptionId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        // 최근 청구일 순으로 조회
        return paymentRepository.findRecentPayments(memberId, subscriptionId, PageRequest.of(0, size));
    }
}
