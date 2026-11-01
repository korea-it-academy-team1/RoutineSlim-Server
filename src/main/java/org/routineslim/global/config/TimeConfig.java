package org.routineslim.global.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 시각 기준과 스케줄러 설정.
 * '오늘'은 한국 시간으로 판단한다 (결제일이 날짜 단위라서).
 * 테스트에서는 Clock 을 고정값으로 바꿔 끼울 수 있다.
 */
@Configuration
@EnableScheduling
public class TimeConfig {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }
}
