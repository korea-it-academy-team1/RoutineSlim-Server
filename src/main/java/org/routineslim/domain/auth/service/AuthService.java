package org.routineslim.domain.auth.service;

import org.routineslim.domain.auth.dto.LoginRequestDTO;
import org.routineslim.domain.auth.dto.SignupRequestDTO;

import java.util.UUID;

public interface AuthService {

    // 회원가입
    UUID signup(SignupRequestDTO signupRequestDTO);

    // 로그인
    UUID login(LoginRequestDTO loginRequestDTO);

}
