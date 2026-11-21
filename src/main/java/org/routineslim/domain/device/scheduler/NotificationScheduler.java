package org.routineslim.domain.device.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    // 매분 0초에 실행 (한국 시간 기준)
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void sendDailyNotifications() {

        // 한국 시간 기준 오늘 날짜
        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Seoul")
        );

        // 스케줄러 실행 확인
        log.info("결제 알림 스케줄러 실행 - 날짜: {}", today);

        // 오늘 알림 대상 구독 개수 조회
        int targetCount = notificationService
                .getNotificationTargets(today)
                .size();

        log.info("오늘 결제 알림 대상 구독 수: {}", targetCount);

//        // 결제 예정 알림 발송
//        notificationService.sendPaymentNotifications(today);
    }

}
