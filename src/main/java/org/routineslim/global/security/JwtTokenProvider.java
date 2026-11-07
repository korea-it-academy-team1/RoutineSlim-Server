package org.routineslim.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    // JWT 서명에 사용할 비밀키
    private final SecretKey secretKey;

    // 토큰 유효시간: 1시간
    private final long expirationTime = 1000L * 60 * 60;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
    // JWT 토큰 생성 (JWT 생성)
    public String createToken(UUID memberId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + expirationTime
        );

        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // JWT 토큰 검증 (JWT가 유효한지 확인)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // JWT에서 회원 UUID 추출 (JWT에서 회원 UUID 추출)
    public UUID getMemberId(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }
}
