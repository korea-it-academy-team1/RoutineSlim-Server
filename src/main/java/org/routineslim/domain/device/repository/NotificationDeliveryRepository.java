package org.routineslim.domain.device.repository;

import org.routineslim.domain.device.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long>{

    // 동일 구독, 결제 예정일, 기기의 발송 이력 존재 여부 확인
    boolean existsBySubscriptionIdAndBillingDateAndDeviceId(
            UUID subscriptionId,
            LocalDate billingDate,
            Long deviceId
    );
}
