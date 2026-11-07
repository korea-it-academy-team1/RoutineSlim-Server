package org.routineslim.domain.user.service;

import org.routineslim.domain.user.dto.UserDTO;

import java.util.UUID;

public interface UserService {

    // 회원정보 조회
    UserDTO getMyInfo(UUID memberId);

    // 회원정보 수정
    void updateMyInfo(UUID memberId, UserDTO userDTO);

    // 회원 탈퇴
    void withdrawUser(UUID memberId);
}
