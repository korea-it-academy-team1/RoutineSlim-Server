package org.routineslim.domain.device.repository;

import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationSubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // 알림이 활성화되어 있고 삭제되지 않은 구독 조회
    List<Subscription> findByNotifyEnabledTrueAndStatusInAndDeletedAtIsNull(
            Collection<SubscriptionStatus> statuses
    );

}
