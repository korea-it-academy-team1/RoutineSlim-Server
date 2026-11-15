package org.routineslim.domain.device.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenRequestDTO {

    // 스마트폰의 FCM 토큰
    private String fcmToken;

    // 알림 수신 여부
    private Boolean notificationEnabled;

}
