package org.routineslim.domain.device.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.entity.Device;
import org.routineslim.domain.device.repository.DeviceRepository;
import org.routineslim.domain.device.repository.NotificationDeliveryRepository;
import org.routineslim.domain.device.repository.NotificationSubscriptionRepository;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
    // 결제 알림 발송 이력 조회 및 저장
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    // 결제 알림 발송 이력 관리
    private final NotificationDeliveryService notificationDeliveryService;

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

            // 회원의 알림 수신 가능 기기 목록 조회
            List<Device> devices  = getNotificationDevices(memberId);

            // 알림 제목 생성
            String title = "RoutineSlim 결제 알림";

            // 알림 내용 생성
            String body = "결제 예정일은 "
                    + subscription.getNextBillingDate()
                    + "입니다.";

            // 회원이 등록한 기기에 알림 발송
            for (Device  device  : devices) {

                // 발송 이력 번호
                Long deliveryId;

                // Firebase 전송 전에 발송 이력 등록
                try {
                    deliveryId = notificationDeliveryService.createProcessingDelivery(
                            subscription.getId(),
                            subscription.getNextBillingDate(),
                            device.getDeviceId()
                    );

                } catch (DataIntegrityViolationException e) {
                    // 발송 이력 중복 저장 시 예외 처리

                    // 동일한 발송 이력이 이미 존재하는지 확인
                    boolean alreadyExists = notificationDeliveryRepository
                            .existsBySubscriptionIdAndBillingDateAndDeviceId(
                                    subscription.getId(),
                                    subscription.getNextBillingDate(),
                                    device.getDeviceId()
                            );

                    if (!alreadyExists) {
                        throw e;
                    }

                    // 중복 알림은 전송하지 않음
                    log.info("중복 알림 건너뛰기 - 구독 ID: {}, 기기 ID: {}",
                            subscription.getId(), device.getDeviceId());

                    continue;
                }

                // Firebase 알림 전송
                try {
                    sendPushNotification(device.getFcmToken(), title, body);

                } catch (Exception e) {

                    // 3. 전송 실패 시 FAILED로 변경
                    log.error("결제 알림 전송 실패 - 회원 ID: {}, 기기 ID: {}",
                            memberId, device.getDeviceId(), e);

                    notificationDeliveryService.markAsFailed(deliveryId);
                    continue;
                }

                // Firebase 전송 성공 시 SENT로 변경
                notificationDeliveryService.markAsSent(deliveryId);
            }
        }
    }

    // 회원의 알림 수신 가능 기기 조회
    private List<Device> getNotificationDevices(UUID memberId) {

        // 회원이 등록한 기기 조회
        return deviceRepository.findByMemberId(memberId)
                .stream()

                // 알림 수신을 허용한 기기만 조회
                .filter(device ->
                        Boolean.TRUE.equals(device.getNotificationEnabled())
                )

                // FCM 토큰이 없는 기기 제외
                .filter(device ->
                        device.getFcmToken() != null
                                && !device.getFcmToken().isBlank()
                )

                // 토큰 목록 반환
                .toList();
    }
}
