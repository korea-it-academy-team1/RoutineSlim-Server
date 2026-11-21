package org.routineslim.domain.device.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.routineslim.domain.device.entity.Device;
import org.routineslim.domain.device.repository.DeviceRepository;
import org.routineslim.domain.device.repository.NotificationDeliveryRepository;
import org.routineslim.domain.device.repository.NotificationSubscriptionRepository;
import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationSubscriptionRepository notificationSubscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    @Spy
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void sendPaymentNotificationsSuccessTest() {

        // 1. 테스트 데이터 준비
        LocalDate today = LocalDate.of(2026, 11, 21);
        LocalDate billingDate = LocalDate.of(2026, 11, 25);

        UUID subscriptionId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        // 2. 테스트용 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getId()).thenReturn(subscriptionId);
        when(subscription.getMemberId()).thenReturn(memberId);
        when(subscription.getNextBillingDate()).thenReturn(billingDate);

        // 3. 테스트용 기기 객체 생성
        Device device = Device.builder()
                .deviceId(1L)
                .memberId(memberId)
                .fcmToken("test-fcm-token")
                .notificationEnabled(true)
                .build();

        // 4. 오늘 알림 대상 구독 조회 결과 설정
        doReturn(List.of(subscription))
                .when(notificationService)
                .getNotificationTargets(today);

        // 5. 회원의 기기 조회 결과 설정
        when(deviceRepository.findByMemberId(memberId))
                .thenReturn(List.of(device));

        // 6. 발송 이력 생성 결과 설정
        when(notificationDeliveryService.createProcessingDelivery(
                subscriptionId,
                billingDate,
                1L
        )).thenReturn(1L);

        // 7. 실제 Firebase 전송 방지
        doNothing()
                .when(notificationService)
                .sendPushNotification(
                        anyString(),
                        anyString(),
                        anyString()
                );

        // 8. 결제 알림 발송 메서드 실행
        notificationService.sendPaymentNotifications(today);

        // 9. 발송 이력 생성 확인
        verify(notificationDeliveryService)
                .createProcessingDelivery(
                        subscriptionId,
                        billingDate,
                        1L
                );

        // 10. Firebase 전송 메서드 호출 확인
        verify(notificationService)
                .sendPushNotification(
                        eq("test-fcm-token"),
                        eq("RoutineSlim 결제 알림"),
                        eq("결제 예정일은 2026-11-25입니다.")
                );

        // 11. SENT 상태 변경 확인
        verify(notificationDeliveryService)
                .markAsSent(1L);

        // 12. FAILED 상태로 변경되지 않았는지 확인
        verify(notificationDeliveryService, never())
                .markAsFailed(anyLong());
    }

    @Test
    void sendPaymentNotificationsFailedTest() {

        // 1. 테스트 데이터 준비
        LocalDate today = LocalDate.of(2026, 11, 21);
        LocalDate billingDate = LocalDate.of(2026, 11, 25);

        UUID subscriptionId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        // 2. 테스트용 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getId()).thenReturn(subscriptionId);
        when(subscription.getMemberId()).thenReturn(memberId);
        when(subscription.getNextBillingDate()).thenReturn(billingDate);

        // 3. 테스트용 기기 객체 생성
        Device device = Device.builder()
                .deviceId(1L)
                .memberId(memberId)
                .fcmToken("test-fcm-token")
                .notificationEnabled(true)
                .build();

        // 4. 알림 대상 구독 조회 결과 설정
        doReturn(List.of(subscription))
                .when(notificationService)
                .getNotificationTargets(today);

        // 5. 회원의 기기 조회 결과 설정
        when(deviceRepository.findByMemberId(memberId))
                .thenReturn(List.of(device));

        // 6. 발송 이력 생성 결과 설정
        when(notificationDeliveryService.createProcessingDelivery(
                subscriptionId,
                billingDate,
                1L
        )).thenReturn(1L);

        // 7. Firebase 전송 실패 상황 만들기
        doThrow(new IllegalStateException("FCM 전송 실패"))
                .when(notificationService)
                .sendPushNotification(
                        anyString(),
                        anyString(),
                        anyString()
                );

        // 8. 결제 알림 발송 실행
        notificationService.sendPaymentNotifications(today);

        // 9. FAILED 상태 변경 확인
        verify(notificationDeliveryService)
                .markAsFailed(1L);

        // 10. SENT 상태로 변경되지 않았는지 확인
        verify(notificationDeliveryService, never())
                .markAsSent(anyLong());
    }

    @Test
    void sendPaymentNotificationsDuplicateTest() {

        // 1. 테스트 데이터 준비
        LocalDate today = LocalDate.of(2026, 11, 21);
        LocalDate billingDate = LocalDate.of(2026, 11, 25);

        UUID subscriptionId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        // 2. 테스트용 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getId()).thenReturn(subscriptionId);
        when(subscription.getMemberId()).thenReturn(memberId);
        when(subscription.getNextBillingDate()).thenReturn(billingDate);

        // 3. 테스트용 기기 생성
        Device device = Device.builder()
                .deviceId(1L)
                .memberId(memberId)
                .fcmToken("test-fcm-token")
                .notificationEnabled(true)
                .build();

        // 4. 알림 대상 구독 조회 결과 설정
        doReturn(List.of(subscription))
                .when(notificationService)
                .getNotificationTargets(today);

        // 5. 기기 조회 결과 설정
        when(deviceRepository.findByMemberId(memberId))
                .thenReturn(List.of(device));

        // 6. 발송 이력 중복 저장 오류 발생
        when(notificationDeliveryService.createProcessingDelivery(
                subscriptionId,
                billingDate,
                1L
        )).thenThrow(new DataIntegrityViolationException("중복 발송 이력"));

        // 7. DB에 동일한 발송 이력이 이미 존재한다고 설정
        when(notificationDeliveryRepository
                .existsBySubscriptionIdAndBillingDateAndDeviceId(
                        subscriptionId,
                        billingDate,
                        1L
                )).thenReturn(true);

        // 8. 결제 알림 발송 실행
        notificationService.sendPaymentNotifications(today);

        // 9. Firebase 전송 메서드가 호출되지 않았는지 확인
        verify(notificationService, never())
                .sendPushNotification(
                        anyString(),
                        anyString(),
                        anyString()
                );

        // 10. 발송 상태 변경도 실행되지 않았는지 확인
        verify(notificationDeliveryService, never())
                .markAsSent(anyLong());

        verify(notificationDeliveryService, never())
                .markAsFailed(anyLong());
    }

    @Test
    void getNotificationTargetsTest() {

        // 1. 테스트 날짜 설정
        LocalDate today = LocalDate.of(2026, 11, 22);
        LocalDate billingDate = LocalDate.of(2026, 11, 25);

        UUID memberId = UUID.randomUUID();

        // 2. 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getMemberId()).thenReturn(memberId);
        when(subscription.getNextBillingDate()).thenReturn(billingDate);
        when(subscription.getNotifyDaysBefore()).thenReturn(3);

        // 3. 회원 객체 생성
        User user = mock(User.class);

        // 4. 구독 조회 결과 설정
        when(notificationSubscriptionRepository
                .findByNotifyEnabledTrueAndStatusInAndDeletedAtIsNull(
                        anyList()
                ))
                .thenReturn(List.of(subscription));

        // 5. 회원 조회 결과 설정
        when(userRepository.findByMemberIdAndDeletedAtIsNull(memberId))
                .thenReturn(Optional.of(user));

        // 6. 실제 알림 대상 조회 메서드 실행
        List<Subscription> result =
                notificationService.getNotificationTargets(today);

        // 7. 결과 확인
        assertEquals(1, result.size());
        assertEquals(subscription, result.get(0));
    }

    @Test
    void getNotificationTargetsDefaultDaysTest() {

        // 1. 테스트 날짜 설정
        LocalDate today = LocalDate.of(2026, 11, 22);
        LocalDate billingDate = LocalDate.of(2026, 11, 25);

        UUID memberId = UUID.randomUUID();

        // 2. 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getMemberId()).thenReturn(memberId);
        when(subscription.getNextBillingDate()).thenReturn(billingDate);

        // 구독별 알림 설정이 없는 상황
        when(subscription.getNotifyDaysBefore()).thenReturn(null);

        // 3. 회원 객체 생성
        User user = mock(User.class);

        // 회원 기본 알림 설정: 3일 전
        when(user.getDefaultNotifyDays()).thenReturn(3);

        // 4. 구독 조회 결과 설정
        when(notificationSubscriptionRepository
                .findByNotifyEnabledTrueAndStatusInAndDeletedAtIsNull(
                        anyList()
                ))
                .thenReturn(List.of(subscription));

        // 5. 회원 조회 결과 설정
        when(userRepository.findByMemberIdAndDeletedAtIsNull(memberId))
                .thenReturn(Optional.of(user));

        // 6. 알림 대상 조회 실행
        List<Subscription> result =
                notificationService.getNotificationTargets(today);

        // 7. 결과 확인
        assertEquals(1, result.size());
        assertEquals(subscription, result.get(0));

        // 8. 회원 기본 알림 설정을 사용했는지 확인
        verify(user).getDefaultNotifyDays();
    }

    @Test
    void getNotificationTargetsNotTodayTest() {

        // 1. 테스트 날짜 설정
        LocalDate today = LocalDate.of(2026, 11, 21);
        LocalDate billingDate = LocalDate.of(2026, 11, 25);

        UUID memberId = UUID.randomUUID();

        // 2. 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getMemberId()).thenReturn(memberId);
        when(subscription.getNextBillingDate()).thenReturn(billingDate);
        when(subscription.getNotifyDaysBefore()).thenReturn(3);

        // 3. 회원 객체 생성
        User user = mock(User.class);

        // 4. 구독 조회 결과 설정
        when(notificationSubscriptionRepository
                .findByNotifyEnabledTrueAndStatusInAndDeletedAtIsNull(
                        anyList()
                ))
                .thenReturn(List.of(subscription));

        // 5. 회원 조회 결과 설정
        when(userRepository.findByMemberIdAndDeletedAtIsNull(memberId))
                .thenReturn(Optional.of(user));

        // 6. 알림 대상 조회 실행
        List<Subscription> result =
                notificationService.getNotificationTargets(today);

        // 7. 알림 대상이 없는지 확인
        assertTrue(result.isEmpty());
    }

    @Test
    void getNotificationTargetsWithdrawnUserTest() {

        // 1. 테스트 날짜 설정
        LocalDate today = LocalDate.of(2026, 11, 22);

        UUID memberId = UUID.randomUUID();

        // 2. 구독 객체 생성
        Subscription subscription = mock(Subscription.class);

        when(subscription.getMemberId()).thenReturn(memberId);

        // 3. 알림이 활성화된 구독 조회 결과 설정
        when(notificationSubscriptionRepository
                .findByNotifyEnabledTrueAndStatusInAndDeletedAtIsNull(
                        anyList()
                ))
                .thenReturn(List.of(subscription));

        // 4. 탈퇴했거나 존재하지 않는 회원 조회 결과
        when(userRepository.findByMemberIdAndDeletedAtIsNull(memberId))
                .thenReturn(Optional.empty());

        // 5. 알림 대상 조회 실행
        List<Subscription> result =
                notificationService.getNotificationTargets(today);

        // 6. 알림 대상에서 제외되었는지 확인
        assertTrue(result.isEmpty());

        // 7. 결제일 조회까지 진행하지 않았는지 확인
        verify(subscription, never()).getNextBillingDate();
    }
}