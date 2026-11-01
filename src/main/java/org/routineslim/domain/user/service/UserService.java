package org.routineslim.domain.user.service;

import org.routineslim.domain.user.dto.UserResponseDTO;

import java.util.UUID;

/* [로그인 - 이대수] 회원 정보 조회 */
public interface UserService {

    // 회원 식별자로 회원 정보 조회 (탈퇴한 회원은 없는 것으로 본다)
    UserResponseDTO getUser(UUID memberId);
}
