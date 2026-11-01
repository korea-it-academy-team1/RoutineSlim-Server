package org.routineslim.domain.subscription.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 청구 기록 (payment 테이블).
 * 결제일마다 1건씩 쌓아서, 요금이 바뀌어도 지난달 통계가 그대로 남게 한다.
 */
@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "sub_id", columnDefinition = "binary(16)", nullable = false, updatable = false)
    private UUID subscriptionId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id", columnDefinition = "binary(16)", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "billed_on", nullable = false)
    private LocalDate billedOn;

    @Column(name = "amount", nullable = false, columnDefinition = "int unsigned")
    private Integer amount;

    @Column(name = "currency", nullable = false, columnDefinition = "char(3)")
    private String currency;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "source", nullable = false, columnDefinition = "varchar(10)")
    private PaymentSource source;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    /** 결제일이 지난 구독의 그때 금액으로 청구 기록을 만든다 */
    public static Payment autoRecord(Subscription subscription, LocalDate billedOn, LocalDateTime now) {
        Payment payment = new Payment();
        payment.subscriptionId = subscription.getId();
        payment.memberId = subscription.getMemberId();
        payment.billedOn = billedOn;
        payment.amount = subscription.getAmount();
        payment.currency = subscription.getCurrency();
        payment.source = PaymentSource.AUTO;
        payment.createdAt = now;
        return payment;
    }
}
