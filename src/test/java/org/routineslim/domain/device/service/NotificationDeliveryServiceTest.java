package org.routineslim.domain.device.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.routineslim.domain.device.entity.NotificationDelivery;
import org.routineslim.domain.device.entity.enums.NotificationDeliveryStatus;
import org.routineslim.domain.device.repository.NotificationDeliveryRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @InjectMocks
    private NotificationDeliveryServiceImpl notificationDeliveryService;

    @Test
    void createProcessingDeliveryTest() {

        // 1. 테스트 데이터 준비
        UUID subscriptionId = UUID.randomUUID();
        LocalDate billingDate = LocalDate.of(2026, 11, 25);
        Long deviceId = 1L;

        // 2. Repository가 저장한 결과를 가짜로 설정
        when(notificationDeliveryRepository.saveAndFlush(
                any(NotificationDelivery.class)
        )).thenAnswer(invocation -> {

            NotificationDelivery delivery = invocation.getArgument(0);
            delivery.setDeliveryId(1L);

            return delivery;
        });

        // 3. 발송 이력 생성 메서드 실행
        Long deliveryId = notificationDeliveryService
                .createProcessingDelivery(
                        subscriptionId,
                        billingDate,
                        deviceId
                );

        // 4. 반환된 발송 이력 번호 확인
        assertEquals(1L, deliveryId);

        // 저장할 때 전달한 발송 이력 객체 확인
        ArgumentCaptor<NotificationDelivery> captor =
                ArgumentCaptor.forClass(NotificationDelivery.class);

        verify(notificationDeliveryRepository)
                .saveAndFlush(captor.capture());

        NotificationDelivery savedDelivery = captor.getValue();

        // PROCESSING 상태로 생성됐는지 확인
        assertEquals(
                NotificationDeliveryStatus.PROCESSING,
                savedDelivery.getStatus()
        );

        // 구독 ID 확인
        assertEquals(subscriptionId, savedDelivery.getSubscriptionId());

        // 결제 예정일 확인
        assertEquals(billingDate, savedDelivery.getBillingDate());

        // 기기 ID 확인
        assertEquals(deviceId, savedDelivery.getDeviceId());
    }

    @Test
    void markAsSentTest() {

        // 1. 테스트용 발송 이력 생성
        Long deliveryId = 1L;

        NotificationDelivery delivery = NotificationDelivery.builder()
                .deliveryId(deliveryId)
                .status(NotificationDeliveryStatus.PROCESSING)
                .build();

        // 2. Repository 조회 결과 설정
        when(notificationDeliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(delivery));

        // 3. 전송 성공 처리 실행
        notificationDeliveryService.markAsSent(deliveryId);

        // 4. SENT 상태로 변경됐는지 확인
        assertEquals(NotificationDeliveryStatus.SENT, delivery.getStatus());

        // 5. 발송 성공 시간이 기록됐는지 확인
        assertNotNull(delivery.getSentAt());

        // 6. Repository 조회가 한 번 실행됐는지 확인
        verify(notificationDeliveryRepository).findById(deliveryId);
    }

    @Test
    void markAsFailedTest() {

        // 1. 테스트용 발송 이력 생성
        Long deliveryId = 1L;

        NotificationDelivery delivery = NotificationDelivery.builder()
                .deliveryId(deliveryId)
                .status(NotificationDeliveryStatus.PROCESSING)
                .build();

        // 2. Repository 조회 결과 설정
        when(notificationDeliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(delivery));

        // 3. 전송 실패 처리 실행
        notificationDeliveryService.markAsFailed(deliveryId);

        // 4. FAILED 상태로 변경됐는지 확인
        assertEquals(
                NotificationDeliveryStatus.FAILED,
                delivery.getStatus()
        );

        // 5. 발송 성공 시간이 기록되지 않았는지 확인
        assertNull(delivery.getSentAt());

        // 6. Repository 조회 여부 확인
        verify(notificationDeliveryRepository).findById(deliveryId);
    }
}