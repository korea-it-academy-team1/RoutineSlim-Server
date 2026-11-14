package org.routineslim.global.security;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

//import org.springframework.boot.context.properties.ConfigurationProperties;
//
//import java.time.Duration;
//
///**
// * [JWT 로그인 - 이대수] application*.yml 의 jwt.* 설정 값을 담는 객체.
// *
// * <pre>
// * jwt:
// *   secret: ...                       # 서명 키 (32자 이상). prod 는 환경 변수 JWT_SECRET
// *   access-token-validity: 30m        # access token 수명
// *   refresh-token-validity: 14d       # "로그인 상태 유지" 를 켰을 때 refresh token 수명
// *   refresh-token-validity-short: 1d  # "로그인 상태 유지" 를 껐을 때 refresh token 수명
// * </pre>
// *
// * 30m, 14d 처럼 적으면 Spring 이 Duration 으로 바꿔 준다.
// * 이 클래스는 JwtConfig 의 @EnableConfigurationProperties 로 등록된다.
// */
//@ConfigurationProperties(prefix = "jwt")
//public record JwtProperties(
//
//        // 토큰 서명 키 (HS256 은 32byte 이상 필요)
//        String secret,
//
//        // access token 유효 시간
//        Duration accessTokenValidity,
//
//        // 로그인 상태 유지를 켰을 때 refresh token 유효 시간
//        Duration refreshTokenValidity,
//
//        // 로그인 상태 유지를 껐을 때 refresh token 유효 시간
//        Duration refreshTokenValidityShort) {
//}
