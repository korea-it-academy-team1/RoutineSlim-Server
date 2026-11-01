package org.routineslim.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.auth.dto.LoginRequestDTO;
import org.routineslim.domain.auth.dto.SignupRequestDTO;
import org.routineslim.domain.auth.dto.SocialLoginRequestDTO;
import org.routineslim.domain.auth.service.AuthService;
import org.routineslim.domain.auth.service.SocialLoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final SocialLoginService socialLoginService;

    // 회원가입
    @PostMapping("/signup")
    public UUID signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        return authService.signup(signupRequestDTO);
    }

    // 로그인
    @PostMapping("/login")
    public UUID login(@RequestBody LoginRequestDTO loginRequestDTO){
        return authService.login(loginRequestDTO);
    }

    // 소셜 로그인
    @PostMapping("/social/login")
    public UUID socialLogin(@RequestBody SocialLoginRequestDTO socialLoginRequestDTO) {
        return socialLoginService.socialLogin(socialLoginRequestDTO);
    }

}
