package org.routineslim.domain.subscription.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.routineslim.domain.payment.dto.PaymentDTO;
import org.routineslim.domain.subscription.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsBySubscriptionIdAndBilledOn(UUID subscriptionId, LocalDate billedOn);

    // 회원의 기간별 청구 기록 조회 (구독 이름·카테고리·주기·상태 포함, 삭제한 구독은 제외, 청구일 순)
    @Query("""
            select new org.routineslim.domain.payment.dto.PaymentDTO(
                p.subscriptionId, s.name, s.categoryId, p.billedOn, p.amount, p.currency,
                s.billingCycle, s.status)
            from Payment p
            join Subscription s on s.id = p.subscriptionId
            where p.memberId = :memberId
              and p.billedOn between :from and :to
              and s.deletedAt is null
            order by p.billedOn, s.name
            """)
    List<PaymentDTO> findMonthlyPayments(
            @Param("memberId") UUID memberId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 한 구독의 최근 청구 기록 조회 (최근 청구일 순, 개수는 pageable 로 제한)
    @Query("""
            select new org.routineslim.domain.payment.dto.PaymentDTO(
                p.subscriptionId, s.name, s.categoryId, p.billedOn, p.amount, p.currency,
                s.billingCycle, s.status)
            from Payment p
            join Subscription s on s.id = p.subscriptionId
            where p.memberId = :memberId
              and p.subscriptionId = :subscriptionId
            order by p.billedOn desc
            """)
    List<PaymentDTO> findRecentPayments(
            @Param("memberId") UUID memberId,
            @Param("subscriptionId") UUID subscriptionId,
            Pageable pageable
    );
}
