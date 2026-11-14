package org.routineslim.domain.user.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsRequestDTO {

    // 기본 알림 일수
    private Integer defaultNotificationDays;

    // 알림 시간
    private LocalTime notificationTime;
}
