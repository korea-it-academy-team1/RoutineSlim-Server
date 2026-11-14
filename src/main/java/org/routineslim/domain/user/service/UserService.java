package org.routineslim.domain.user.service;

import org.routineslim.domain.user.dto.NotificationSettingsRequestDTO;
import org.routineslim.domain.user.dto.UserDTO;
import org.routineslim.domain.user.dto.UserResponseDTO;

import java.util.UUID;

public interface UserService {

    // 회원정보 조회
    UserDTO getMyInfo(UUID memberId);

    // 회원정보 수정
    void updateMyInfo(UUID memberId, UserDTO userDTO);

    // 회원 탈퇴
    void withdrawUser(UUID memberId);

    // 회원 식별자로 회원 정보 조회 (탈퇴한 회원은 없는 것으로 본다)
    UserResponseDTO getUser(UUID memberId);

    // 알림 수정
    void  updateNotificationSettings(UUID memberId, NotificationSettingsRequestDTO requestDTO);
}
