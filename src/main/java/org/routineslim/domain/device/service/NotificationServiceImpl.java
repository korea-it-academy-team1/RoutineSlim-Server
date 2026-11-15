package org.routineslim.domain.device.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.repository.NotificationSubscriptionRepository;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    // 결제 알림 대상 구독 조회
    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    // 회원 기본 알림 설정 조회
    private final UserRepository userRepository;

    // 알림이 활성화된 구독 조회
    @Override
    public List<Subscription> getNotificationTargets(LocalDate today) {

        // 알림이 활성화된 구독 조회
        List<Subscription> subscriptions =
                notificationSubscriptionRepository
                        .findByNotifyEnabledTrueAndStatusInAndDeletedAtIsNull(
                                List.of(
                                        SubscriptionStatus.ACTIVE,
                                        SubscriptionStatus.TRIAL
                                )
                        );

        // 오늘 알림 대상인 구독만 반환
        return subscriptions.stream()
                .filter(subscription -> {

                    // 구독 회원 조회
                    User user = userRepository
                            .findByMemberIdAndDeletedAtIsNull(subscription.getMemberId())
                            .orElse(null);

                    // 탈퇴했거나 존재하지 않는 회원 제외
                    if (user == null) {
                        return false;
                    }

                    // 구독별 알림 설정 확인
                    Integer notifyDaysBefore = subscription.getNotifyDaysBefore();

                    // 구독별 설정이 없으면 회원 기본값 사용
                    if (notifyDaysBefore == null) {
                        notifyDaysBefore = user.getDefaultNotifyDays();
                    }

                    // 실제 알림 날짜 계산
                    LocalDate notificationDate =
                            subscription.getNextBillingDate()
                                    .minusDays(notifyDaysBefore);

                    // 오늘이 알림 날짜인지 확인
                    return notificationDate.equals(today);

                })
                .toList();
    }
}
