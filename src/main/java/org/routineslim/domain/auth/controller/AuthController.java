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

//// [JWT 로그인 - 이대수] 토큰 발급용 import
//import org.routineslim.domain.auth.dto.RefreshTokenRequestDTO;
//import org.routineslim.domain.auth.dto.TokenResponseDTO;
//import org.routineslim.domain.auth.service.TokenService;

/*
 * [로그인 - 이대수] 지금은 1번 방식: 로그인하면 회원 UUID 를 돌려준다 (팀원 원래 코드).
 * JWT(3번)로 바꿀 때: [1번 임시 - 이대수] 메서드는 주석 처리, [JWT 로그인 - 이대수] 부분은 주석 해제.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final SocialLoginService socialLoginService;

//    // [JWT 로그인 - 이대수] 토큰 발급·재발급·폐기
//    private final TokenService tokenService;

    // 회원가입
    @PostMapping("/signup")
    public UUID signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        return authService.signup(signupRequestDTO);
    }

    // 로그인
    // [1번 임시 - 이대수] 회원 UUID 를 그대로 돌려준다. 앱/웹은 이 값을 저장해 두고 X-Member-Id 헤더로 보낸다
    @PostMapping("/login")
    public UUID login(@RequestBody LoginRequestDTO loginRequestDTO){
        return authService.login(loginRequestDTO);
    }

//    // [JWT 로그인 - 이대수] 아이디·비밀번호 확인은 기존 authService.login 이 하고,
//    // 확인된 회원 식별자로 토큰을 발급해서 돌려준다 (응답: UUID → TokenResponseDTO)
//    @PostMapping("/login")
//    public TokenResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO){
//        UUID memberId = authService.login(loginRequestDTO);
//        return tokenService.issue(memberId, loginRequestDTO.isKeepLoggedIn());
//    }

    // 소셜 로그인
    // [1번 임시 - 이대수] 회원 UUID 를 그대로 돌려준다
    @PostMapping("/social/login")
    public UUID socialLogin(@RequestBody SocialLoginRequestDTO socialLoginRequestDTO) {
        return socialLoginService.socialLogin(socialLoginRequestDTO);
    }

//    // [JWT 로그인 - 이대수] 소셜 토큰 검증·회원 생성은 기존 socialLoginService 가 하고, 이어서 토큰을 발급한다
//    // 소셜 로그인은 "로그인 상태 유지" 선택이 없으므로 유지(14일)로 발급한다
//    @PostMapping("/social/login")
//    public TokenResponseDTO socialLogin(@RequestBody SocialLoginRequestDTO socialLoginRequestDTO) {
//        UUID memberId = socialLoginService.socialLogin(socialLoginRequestDTO);
//        return tokenService.issue(memberId, true);
//    }

//    // [JWT 로그인 - 이대수] 토큰 재발급
//    // access token 이 만료돼서 401 을 받으면 앱/웹이 refresh token 으로 이 API 를 부른다
//    // 응답의 refreshToken 은 새 값이므로 앱/웹은 저장된 값을 바꿔 둬야 한다 (예전 값은 폐기됨)
//    @PostMapping("/refresh")
//    public TokenResponseDTO refresh(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
//        return tokenService.refresh(refreshTokenRequestDTO.getRefreshToken());
//    }

//    // [JWT 로그인 - 이대수] 로그아웃
//    // 서버의 refresh token 을 폐기한다. 앱/웹은 저장해 둔 토큰 두 개를 지운다
//    // (access token 은 서버에 저장하지 않으므로 만료 시각까지는 유효하다 → 수명을 짧게(30분) 둔 이유)
//    @PostMapping("/logout")
//    public void logout(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
//        tokenService.logout(refreshTokenRequestDTO.getRefreshToken());
//    }

}
