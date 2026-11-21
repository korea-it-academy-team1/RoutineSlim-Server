package org.routineslim.domain.device.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.entity.NotificationDelivery;
import org.routineslim.domain.device.entity.enums.NotificationDeliveryStatus;
import org.routineslim.domain.device.repository.NotificationDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService{

    // 결제 알림 발송 이력 저장
    private final NotificationDeliveryRepository notificationDeliveryRepository;

    // Firebase 전송 전 발송 이력 등록
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createProcessingDelivery(UUID subscriptionId, LocalDate billingDate, Long deviceId) {

        // 발송 이력 객체 생성
        NotificationDelivery delivery = NotificationDelivery.builder()
                .subscriptionId(subscriptionId)
                .billingDate(billingDate)
                .deviceId(deviceId)
                .status(NotificationDeliveryStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .build();

        // DB에 발송 이력 저장
        NotificationDelivery saved =
                notificationDeliveryRepository.saveAndFlush(delivery);

        // 생성된 발송 이력 번호 반환
        log.info("결제 알림 발송 이력 등록 - ID: {}", saved.getDeliveryId());

        return saved.getDeliveryId();
    }

    // Firebase 전송 성공 시 상태 변경
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsSent(Long deliveryId) {

        // 발송 이력 번호로 DB 조회
        NotificationDelivery delivery = notificationDeliveryRepository
                .findById(deliveryId)
                .orElseThrow(() ->
                        new IllegalArgumentException("발송 이력을 찾을 수 없습니다.")
                );

        // 전송 성공 상태로 변경
        delivery.setStatus(NotificationDeliveryStatus.SENT);

        // 전송 성공 시간 기록
        delivery.setSentAt(LocalDateTime.now());

        log.info("결제 알림 전송 성공 처리 - ID: {}", deliveryId);
    }

    // Firebase 전송 실패 시 상태 변경
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markAsFailed(Long deliveryId) {

        // 발송 이력 번호로 DB 조회
        NotificationDelivery delivery = notificationDeliveryRepository
                .findById(deliveryId)
                .orElseThrow(() ->
                        new IllegalArgumentException("발송 이력을 찾을 수 없습니다.")
                );

        // 전송 실패 상태로 변경
        delivery.setStatus(NotificationDeliveryStatus.FAILED);

        log.warn("결제 알림 전송 실패 처리 - ID: {}", deliveryId);
    }
}
