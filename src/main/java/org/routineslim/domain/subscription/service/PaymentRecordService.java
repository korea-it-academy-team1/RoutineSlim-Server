package org.routineslim.domain.subscription.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.routineslim.domain.subscription.entity.Payment;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.subscription.repository.PaymentRepository;
import org.routineslim.domain.subscription.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제일이 된 구독의 청구 기록을 남기고 다음 결제일로 넘기는 배치.
 * 매일 00:05(한국 시간)에 실행한다. 서버가 며칠 꺼져 있었어도 밀린 결제일을 모두 채운다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecordService {

    private static final List<SubscriptionStatus> TARGET_STATUSES =
            List.of(SubscriptionStatus.TRIAL, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED);

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void recordDuePayments() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);

        List<Subscription> dueSubscriptions =
                subscriptionRepository.findByStatusInAndNextBillingDateLessThanEqualAndDeletedAtIsNull(
                        TARGET_STATUSES, today);

        int recorded = 0;
        for (Subscription subscription : dueSubscriptions) {
            while (!subscription.getNextBillingDate().isAfter(today)) {
                LocalDate billedOn = subscription.getNextBillingDate();
                boolean charged = subscription.passBillingDate(now);
                if (charged && !paymentRepository.existsBySubscriptionIdAndBilledOn(subscription.getId(), billedOn)) {
                    paymentRepository.save(Payment.autoRecord(subscription, billedOn, now));
                    recorded++;
                }
            }
        }
        log.info("청구 기록 배치: 대상 구독 {}건, 새 청구 기록 {}건", dueSubscriptions.size(), recorded);
    }
}
