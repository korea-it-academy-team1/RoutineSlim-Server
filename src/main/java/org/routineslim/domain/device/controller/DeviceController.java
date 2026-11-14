package org.routineslim.domain.device.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.dto.DeviceTokenRequestDTO;
import org.routineslim.domain.device.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DeviceTokenRequestDTO deviceTokenRequestDTO
    ) {

        // JWT에서 로그인한 회원 UUID 추출
        UUID memberId = UUID.fromString(jwt.getSubject());

        // 기기 토큰 등록 및 갱신
        deviceService.registerDevice(memberId, deviceTokenRequestDTO);

        // 정상 처리 시 HTTP 204 반환
        return ResponseEntity.noContent().build();
    }
}
