package org.routineslim.domain.device.service;

import org.routineslim.domain.device.dto.DeviceTokenRequestDTO;

import java.util.UUID;

public interface DeviceService {

    // 기기 FCM 토큰 등록 및 갱신
    void registerDevice(UUID memberId, DeviceTokenRequestDTO deviceTokenRequestDTO);

}
