package org.routineslim.domain.device.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.device.dto.DeviceTokenRequestDTO;
import org.routineslim.domain.device.entity.Device;
import org.routineslim.domain.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class DeviceServiceImpl implements DeviceService{

    private final DeviceRepository deviceRepository;


    // FCM 토큰 등록 및 갱신
    @Override
    public void registerDevice(UUID memberId, DeviceTokenRequestDTO deviceTokenRequestDTO) {

        // FCM 토큰 확인
        String fcmToken = deviceTokenRequestDTO.getFcmToken();

        if (fcmToken == null || fcmToken.isBlank()) {
            throw new IllegalArgumentException("FCM 토큰은 필수입니다.");
        }

        // 알림 수신 여부 확인
        Boolean notificationEnabled = deviceTokenRequestDTO.getNotificationEnabled();

        if (notificationEnabled == null) {
            throw new IllegalArgumentException("알림 수신 여부는 필수입니다.");
        }

        // 기존 FCM 토큰 조회
        Device device = deviceRepository.findByFcmToken(fcmToken)
                .orElse(null);

        if (device != null) {

            // 다른 회원의 토큰이면 등록 차단
            if (!device.getMemberId().equals(memberId)) {
                throw new IllegalStateException("다른 회원에게 등록된 기기입니다.");
            }

            // 기존 기기의 알림 설정 수정
            device.setNotificationEnabled(notificationEnabled);

        } else {

            // 새로운 기기 등록
            device = Device.builder()
                    .memberId(memberId)
                    .fcmToken(fcmToken)
                    .notificationEnabled(notificationEnabled)
                    .build();
        }

        // DB 저장
        deviceRepository.save(device);

    }
}
