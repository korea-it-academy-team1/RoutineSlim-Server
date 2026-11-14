package org.routineslim.domain.auth.service;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

//import lombok.RequiredArgsConstructor;
//import org.routineslim.domain.auth.dto.TokenResponseDTO;
//import org.routineslim.domain.auth.entity.RefreshToken;
//import org.routineslim.domain.auth.repository.RefreshTokenRepository;
//import org.routineslim.domain.user.entity.User;
//import org.routineslim.domain.user.repository.UserRepository;
//import org.routineslim.global.security.JwtProperties;
//import org.routineslim.global.security.JwtTokenProvider;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.Base64;
//import java.util.HexFormat;
//import java.util.UUID;
//
//// [JWT 로그인 - 이대수] TokenService 구현
//@RequiredArgsConstructor
//@Service
//@Transactional
//public class TokenServiceImpl implements TokenService {
//
//    // refresh token 은 JWT 가 아니라 추측할 수 없는 임의 문자열 (32byte = 256bit)
//    private static final SecureRandom RANDOM = new SecureRandom();
//    private static final int REFRESH_TOKEN_BYTES = 32;
//
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final UserRepository userRepository;
//    private final JwtTokenProvider jwtTokenProvider;
//    private final JwtProperties jwtProperties;
//
//    // 토큰 발급
//    @Override
//    public TokenResponseDTO issue(UUID memberId, boolean keepLoggedIn) {
//
//        // 회원 조회 (DB 를 한 번 더 조회하지 않고 member_id 만 가진 참조 객체를 쓴다)
//        User member = userRepository.getReferenceById(memberId);
//
//        // 로그인 상태 유지 여부에 따라 refresh token 만료 시각 결정
//        Instant now = Instant.now();
//        Duration validity = keepLoggedIn
//                ? jwtProperties.refreshTokenValidity()
//                : jwtProperties.refreshTokenValidityShort();
//
//        return issueTokens(member, now, toLocal(now.plus(validity)));
//    }
//
//    // 토큰 재발급
//    @Override
//    public TokenResponseDTO refresh(String refreshToken) {
//        Instant now = Instant.now();
//
//        // 보낸 토큰을 해시해서 DB 에서 찾고, 폐기·만료되지 않았는지 확인
//        RefreshToken saved = refreshTokenRepository.findByTokenHash(hash(refreshToken))
//                .filter(token -> token.isUsable(toLocal(now)))
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."));
//
//        // 사용한 토큰은 폐기한다 (rotation)
//        // → 토큰이 탈취돼도 한 번밖에 못 쓰고, 정상 사용자가 먼저 쓰면 탈취한 쪽은 쓸 수 없다
//        saved.revoke(toLocal(now));
//
//        // 새 refresh token 의 만료 시각은 처음 로그인 때 정한 시각을 그대로 쓴다
//        // (재발급을 계속해도 로그인 유지 기간이 끝없이 늘어나지 않게)
//        return issueTokens(saved.getMember(), now, saved.getExpiresAt());
//    }
//
//    // 로그아웃
//    @Override
//    public void logout(String refreshToken) {
//
//        // 이미 만료·폐기됐거나 없는 토큰이어도 오류 없이 성공으로 처리한다
//        // (앱/웹은 응답과 상관없이 저장해 둔 토큰을 지우면 된다)
//        refreshTokenRepository.findByTokenHash(hash(refreshToken))
//                .ifPresent(token -> token.revoke(LocalDateTime.now()));
//    }
//
//    // access token + refresh token 을 만들고, refresh token 해시를 DB 에 저장
//    private TokenResponseDTO issueTokens(User member, Instant now, LocalDateTime refreshExpiresAt) {
//        String refreshToken = newRefreshToken();
//
//        refreshTokenRepository.save(
//                new RefreshToken(member, hash(refreshToken), refreshExpiresAt, toLocal(now)));
//
//        return TokenResponseDTO.builder()
//                .accessToken(jwtTokenProvider.createAccessToken(member.getMemberId(), now))
//                .expiresIn(jwtTokenProvider.accessTokenValiditySeconds())
//                .refreshToken(refreshToken)
//                .build();
//    }
//
//    // URL 에 넣어도 안전한 Base64 문자열로 임의 토큰 생성
//    private static String newRefreshToken() {
//        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
//        RANDOM.nextBytes(bytes);
//        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
//    }
//
//    // 토큰 원문 → SHA-256 해시 (16진수 64자). DB 에는 이 값만 저장한다
//    private static String hash(String token) {
//        if (token == null || token.isBlank()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refresh token 이 없습니다.");
//        }
//        try {
//            byte[] digest = MessageDigest.getInstance("SHA-256")
//                    .digest(token.getBytes(StandardCharsets.UTF_8));
//            return HexFormat.of().formatHex(digest);
//        } catch (NoSuchAlgorithmException e) {
//            // SHA-256 은 모든 JDK 에 있으므로 실제로는 일어나지 않는다
//            throw new IllegalStateException(e);
//        }
//    }
//
//    // DB 의 DATETIME 컬럼에 맞게 서버 시간대의 LocalDateTime 으로 변환
//    private static LocalDateTime toLocal(Instant instant) {
//        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//    }
//}
