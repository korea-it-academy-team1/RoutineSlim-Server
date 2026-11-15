package org.routineslim.domain.device.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.dto.DeviceTokenRequestDTO;
import org.routineslim.domain.device.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/device")
public class DeviceController {

    private final DeviceService deviceService;

    // FCM 토큰 등록 및 갱신
    @PostMapping("/token")
    public ResponseEntity<Void> registerDevice(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestBody DeviceTokenRequestDTO deviceTokenRequestDTO
    ) {

        // 기기 토큰 등록 및 갱신
        deviceService.registerDevice(memberId, deviceTokenRequestDTO);

        // 정상 처리 시 HTTP 204 반환
        return ResponseEntity.noContent().build();
    }
}
