package org.routineslim.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.payment.dto.MonthlyPaymentResponseDTO;
import org.routineslim.domain.payment.dto.PaymentDTO;
import org.routineslim.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    // 월별 청구 기록 조회 (통계·캘린더 화면의 지난달 데이터)
    // 예) GET /payment/monthly?yearMonth=2026-10
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyPaymentResponseDTO> getMonthlyPayments(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestParam("yearMonth") String yearMonth
    ) {

        // 그 달의 청구 기록과 합계 조회
        MonthlyPaymentResponseDTO response = paymentService.getMonthlyPayments(memberId, yearMonth);

        // 정상 처리 시 HTTP 200 반환
        return ResponseEntity.ok(response);
    }

    // 한 구독의 최근 청구 기록 조회 (내 구독 관리 > 구독 상세)
    // 예) GET /payment/subscription/{subscriptionId}?limit=6
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<PaymentDTO>> getSubscriptionPayments(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("subscriptionId") UUID subscriptionId,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {

        // 최근 청구 기록 조회
        List<PaymentDTO> response = paymentService.getSubscriptionPayments(memberId, subscriptionId, limit);

        // 정상 처리 시 HTTP 200 반환
        return ResponseEntity.ok(response);
    }
}
