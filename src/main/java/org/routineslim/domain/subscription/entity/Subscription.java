package org.routineslim.domain.subscription.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.routineslim.global.util.BillingDateCalculator;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 (subscription 테이블).
 * 카테고리·프리셋·카드는 다른 도메인이라 연관관계 대신 ID만 들고 있는다.
 * 시각 컬럼은 DB 기본값에 기대지 않고 서비스에서 Clock 기준으로 넣는다.
 */
@Entity
@Table(name = "subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription implements Persistable<UUID> {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "sub_id", columnDefinition = "binary(16)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id", columnDefinition = "binary(16)", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "sub_ctg_id", nullable = false)
    private Long categoryId;

    @Column(name = "preset_id")
    private Long presetId;

    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "amount", nullable = false, columnDefinition = "int unsigned")
    private Integer amount;

    @Column(name = "currency", nullable = false, columnDefinition = "char(3)")
    private String currency;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "billing_cycle", nullable = false, columnDefinition = "varchar(10)")
    private BillingCycle billingCycle;

    @Column(name = "cycle_interval", nullable = false, columnDefinition = "smallint unsigned")
    private Integer cycleInterval;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 원래 결제일 (1~31). 말일 보정 계산의 기준 */
    @Column(name = "billing_day", nullable = false, columnDefinition = "tinyint unsigned")
    private Integer billingDay;

    @Column(name = "next_billing_date", nullable = false)
    private LocalDate nextBillingDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(10)")
    private SubscriptionStatus status;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "notify_enabled", nullable = false, columnDefinition = "tinyint(1)")
    private Boolean notifyEnabled;

    /** NULL 이면 회원 기본값(member.default_notify_days)을 쓴다 */
    @Column(name = "notify_days_before", columnDefinition = "tinyint unsigned")
    private Integer notifyDaysBefore;

    @Column(name = "memo", columnDefinition = "text")
    private String memo;

    @Column(name = "canceled_at", columnDefinition = "datetime(3)")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "datetime(3)")
    private LocalDateTime deletedAt;

    /** ID를 직접 넣는 엔티티라서, save() 가 merge(SELECT 후 INSERT) 대신 persist 하도록 새 엔티티임을 표시한다 */
    @Transient
    private boolean isNew = true;

    public static Subscription create(UUID id, UUID memberId, Details details, SubscriptionStatus status,
                                      LocalDate today, LocalDateTime now) {
        Subscription subscription = new Subscription();
        subscription.id = id;
        subscription.memberId = memberId;
        subscription.status = status;
        subscription.createdAt = now;
        subscription.applyDetails(details, today, now);
        return subscription;
    }

    /** 수정 화면에서 받은 값으로 전부 바꾼다. 결제 일정이 바뀌면 다음 결제일을 다시 계산한다 */
    public void update(Details details, LocalDate today, LocalDateTime now) {
        applyDetails(details, today, now);
    }

    public void changeStatus(SubscriptionStatus newStatus, LocalDate trialEndDate, LocalDate today,
                             LocalDateTime now) {
        if (newStatus == SubscriptionStatus.CANCELED) {
            if (status != SubscriptionStatus.CANCELED) {
                canceledAt = now;
            }
        } else {
            boolean resuming = status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.PAUSED;
            canceledAt = null;
            if (resuming) {
                // 해지·정지 중에 지나간 결제일은 건너뛰고 오늘 이후로 맞춘다
                nextBillingDate = firstBillingOnOrAfter(today);
            }
            if (newStatus == SubscriptionStatus.TRIAL && trialEndDate != null) {
                this.trialEndDate = trialEndDate;
            }
        }
        status = newStatus;
        updatedAt = now;
    }

    public void delete(LocalDateTime now) {
        deletedAt = now;
        updatedAt = now;
    }

    /**
     * 결제일이 지났을 때 배치가 부른다. 다음 결제일로 넘기고, 무료 체험이었다면 ACTIVE 로 바꾼다.
     * @return 청구 기록을 남겨야 하면 true (일시 정지 중이면 false)
     */
    public boolean passBillingDate(LocalDateTime now) {
        boolean charged = status != SubscriptionStatus.PAUSED;
        if (status == SubscriptionStatus.TRIAL) {
            status = SubscriptionStatus.ACTIVE;
        }
        nextBillingDate = BillingDateCalculator.firstAfter(startDate, billingCycle, cycleInterval, billingDay,
                nextBillingDate);
        updatedAt = now;
        return charged;
    }

    public int getMonthlyAmount() {
        return BillingDateCalculator.monthlyAmount(amount, billingCycle, cycleInterval);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    private void applyDetails(Details details, LocalDate today, LocalDateTime now) {
        boolean scheduleChanged = nextBillingDate == null
                || !details.startDate().equals(startDate)
                || details.billingCycle() != billingCycle
                || details.cycleInterval() != cycleInterval.intValue();

        categoryId = details.categoryId();
        presetId = details.presetId();
        cardId = details.cardId();
        name = details.name();
        amount = details.amount();
        currency = details.currency();
        billingCycle = details.billingCycle();
        cycleInterval = details.cycleInterval();
        startDate = details.startDate();
        billingDay = details.startDate().getDayOfMonth();
        trialEndDate = details.trialEndDate();
        notifyEnabled = details.notifyEnabled();
        notifyDaysBefore = details.notifyDaysBefore();
        memo = details.memo();
        updatedAt = now;

        if (scheduleChanged) {
            nextBillingDate = firstBillingOnOrAfter(today);
        }
    }

    private LocalDate firstBillingOnOrAfter(LocalDate date) {
        return BillingDateCalculator.firstOnOrAfter(startDate, billingCycle, cycleInterval, billingDay, date);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }

    /** 사용자가 입력하는 구독 정보 (등록·수정 공통) */
    public record Details(
            String name,
            Long categoryId,
            Long presetId,
            Long cardId,
            int amount,
            String currency,
            BillingCycle billingCycle,
            int cycleInterval,
            LocalDate startDate,
            LocalDate trialEndDate,
            boolean notifyEnabled,
            Integer notifyDaysBefore,
            String memo
    ) {
    }
}
