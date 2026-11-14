package org.routineslim.domain.device.repository;

import org.routineslim.domain.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    // FCM 토큰으로 기기 조회
    Optional<Device> findByFcmToken(String fcmToken);

    // 회원이 등록한 모든 기기 조회
    List<Device> findByMemberId(UUID memberId);
}
