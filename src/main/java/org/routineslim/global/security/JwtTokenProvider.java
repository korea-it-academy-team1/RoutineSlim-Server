package org.routineslim.global.security;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * [JWT 로그인 - 이대수] access token 발급기.
 *
 * 토큰 안의 subject(sub) 에 회원 식별자(member_id, UUID) 를 문자열로 넣는다.
 * 로그인이 필요한 API 컨트롤러에서는 아래처럼 회원 식별자를 꺼낸다.
 *
 * <pre>
 * public ... someApi(@AuthenticationPrincipal Jwt jwt) {
 *     UUID memberId = UUID.fromString(jwt.getSubject());
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // 토큰 발급자 표시 (iss)
    private static final String ISSUER = "routineslim";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    // access token 생성
    public String createAccessToken(UUID memberId, Instant now) {

        // 토큰에 담을 내용 (발급자, 회원 식별자, 발급 시각, 만료 시각)
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(memberId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenValidity()))
                .build();

        // 서명 방식 HS256 (JwtConfig 의 비밀 키로 서명)
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // access token 유효 시간(초). 응답의 expiresIn 으로 프론트에 알려 준다
    public long accessTokenValiditySeconds() {
        return properties.accessTokenValidity().toSeconds();
    }
}
