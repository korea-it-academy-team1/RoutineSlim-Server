package org.routineslim.domain.device.service;

import java.time.LocalDate;
import java.util.UUID;

public interface NotificationDeliveryService {

    // Firebase 전송 전 발송 이력 등록
    Long createProcessingDelivery(UUID subscriptionId, LocalDate billingDate, Long deviceId);

    // Firebase 전송 성공 시 상태 변경
    void markAsSent(Long deliveryId);

    // Firebase 전송 실패 시 상태 변경
    void markAsFailed(Long deliveryId);
}
