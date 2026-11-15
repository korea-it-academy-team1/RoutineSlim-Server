package org.routineslim.domain.device.service;

import org.routineslim.domain.subscription.entity.Subscription;

import java.time.LocalDate;
import java.util.List;

public interface NotificationService {

    // 지정한 날짜에 결제 알림 대상이 되는 구독 조회
    List<Subscription> getNotificationTargets(LocalDate today);
}
