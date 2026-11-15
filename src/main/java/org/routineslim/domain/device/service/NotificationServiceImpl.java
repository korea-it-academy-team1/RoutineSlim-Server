package org.routineslim.domain.device.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.entity.Device;
import org.routineslim.domain.device.repository.DeviceRepository;
import org.routineslim.domain.device.repository.NotificationSubscriptionRepository;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.Notification;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    // 결제 알림 대상 구독 조회
    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    // 회원 기본 알림 설정 조회
    private final UserRepository userRepository;
    // 회원의 기기 정보 조회
    private final DeviceRepository deviceRepository;

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

    // Firebase 푸시 알림 전송
    @Override
    public void sendPushNotification(String fcmToken, String title, String body) {

        // Firebase 알림 내용 생성
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // 전송할 메시지 생성
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .build();

        // Firebase로 메시지 전송
        try {
            String response = FirebaseMessaging.getInstance()
                    .send(message);

            log.info("FCM 알림 전송 성공: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 전송 실패", e);
            throw new IllegalStateException("푸시 알림 전송에 실패했습니다.", e);
        }
    }

    // 결제 예정 구독의 푸시 알림 발송
    @Override
    public void sendPaymentNotifications(LocalDate today) {

        // 오늘 알림 대상 구독 조회
        List<Subscription> subscriptions =
                getNotificationTargets(today);

        // 알림 대상 구독을 하나씩 처리
        for (Subscription subscription : subscriptions) {

            // 구독 회원 ID 조회
            UUID memberId = subscription.getMemberId();

            // 회원의 FCM 토큰 목록 조회
            List<String> fcmTokens = getFcmTokens(memberId);

            // 알림 제목 생성
            String title = "RoutineSlim 결제 알림";

            // 알림 내용 생성
            String body = "결제 예정일은 "
                    + subscription.getNextBillingDate()
                    + "입니다.";

            // 회원이 등록한 기기에 알림 발송
            for (String fcmToken : fcmTokens) {

                try {
                    sendPushNotification(fcmToken, title, body);

                } catch (Exception e) {
                    // 한 기기의 전송 실패가 다른 기기에 영향을 주지 않도록 처리
                    log.error(
                            "결제 알림 전송 실패 - 회원 ID: {}",
                            memberId,
                            e
                    );
                }
            }
        }
    }

    // 회원의 FCM 토큰 조회
    private List<String> getFcmTokens(UUID memberId) {

        // 회원이 등록한 기기 조회
        return deviceRepository.findByMemberId(memberId)
                .stream()

                // 각 기기에서 FCM 토큰 추출
                .map(Device::getFcmToken)

                // 토큰이 없거나 비어 있으면 제외
                .filter(token -> token != null && !token.isBlank())

                // 토큰 목록 반환
                .toList();
    }
}
