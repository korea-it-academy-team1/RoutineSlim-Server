package org.routineslim.global.security;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * [JWT 로그인 - 이대수] access token(JWT) 을 만들고(Encoder) 검사하는(Decoder) 도구를 등록한다.
 *
 * - 서명 방식: HS256 (서버만 아는 비밀 키 하나로 서명·검증)
 * - JwtEncoder: 로그인 성공 시 JwtTokenProvider 가 토큰을 만들 때 사용
 * - JwtDecoder: 요청이 들어올 때 Spring Security 가 "Authorization: Bearer ..." 토큰을 검사할 때 사용
 *   (SecurityConfig 의 oauth2ResourceServer().jwt() 가 이 Bean 을 자동으로 찾아 쓴다)
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    // HS256 은 256bit(=32byte) 이상의 키가 필요하다
    private static final int MIN_SECRET_BYTES = 32;

    // jwt.secret 문자열을 서명 키로 변환
    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        String secret = properties.secret();

        // 키가 없거나 짧으면 서버 시작 단계에서 바로 실패시킨다 (약한 키로 운영되는 것을 막기 위해)
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret(JWT_SECRET) 은 " + MIN_SECRET_BYTES + "자 이상이어야 합니다.");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // 토큰 발급용
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    // 토큰 검증용 (서명이 맞는지 + 만료되지 않았는지 검사)
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
