package org.routineslim.domain.auth.dto;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

import lombok.*;

/**
 * [JWT 로그인 - 이대수] 로그인·소셜 로그인·토큰 재발급 응답.
 *
 * 앱/웹은
 * - 로그인이 필요한 API 를 부를 때 헤더에 "Authorization: Bearer {accessToken}" 을 붙이고
 * - access token 이 만료되면(401) refreshToken 으로 POST /auth/refresh 를 불러 새 토큰을 받는다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDTO {

    // API 호출용 토큰 (JWT)
    private String accessToken;

    // accessToken 유효 시간 (초)
    private long expiresIn;

    // 토큰 재발급용 토큰 (앱/웹이 안전한 곳에 저장)
    private String refreshToken;
}
