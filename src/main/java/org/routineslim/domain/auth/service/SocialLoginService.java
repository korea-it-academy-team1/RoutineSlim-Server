package org.routineslim.domain.auth.service;

import org.routineslim.domain.auth.dto.SocialLoginRequestDTO;

import java.util.UUID;

public interface SocialLoginService {

    // 소셜 로그인
    UUID socialLogin(SocialLoginRequestDTO requestDTO);

}
