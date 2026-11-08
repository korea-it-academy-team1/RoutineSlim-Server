package org.routineslim.domain.auth.service;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

import org.routineslim.domain.auth.dto.TokenResponseDTO;

import java.util.UUID;

/**
 * [JWT 로그인 - 이대수] 토큰 발급·재발급·폐기.
 *
 * 아이디/비밀번호 확인(AuthService)과 소셜 계정 확인(SocialLoginService)은 기존 코드가 하고,
 * 확인이 끝난 회원 식별자로 이 서비스가 토큰을 만들어 준다.
 */
public interface TokenService {

    // 로그인에 성공한 회원에게 access token + refresh token 발급
    TokenResponseDTO issue(UUID memberId, boolean keepLoggedIn);

    // refresh token 으로 새 토큰 발급 (사용한 refresh token 은 폐기)
    TokenResponseDTO refresh(String refreshToken);

    // 로그아웃 (refresh token 폐기)
    void logout(String refreshToken);
}
