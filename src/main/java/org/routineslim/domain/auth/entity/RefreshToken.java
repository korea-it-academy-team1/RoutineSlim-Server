package org.routineslim.domain.auth.entity;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

//import jakarta.persistence.*;
//import lombok.*;
//import org.routineslim.domain.user.entity.User;
//
//import java.time.LocalDateTime;
//
///**
// * [JWT 로그인 - 이대수] refresh token (refresh_token 테이블, V1__init.sql).
// *
// * - access token 은 수명이 짧다(30분). 만료되면 앱/웹이 refresh token 으로 새 토큰을 받는다.
// * - 토큰 원문은 앱/웹에만 저장하고, DB 에는 SHA-256 해시만 저장한다.
// *   (DB 가 유출돼도 해시로는 로그인할 수 없다)
// * - 한 번 사용한 refresh token 은 폐기(revoke)하고 새로 발급한다 (rotation).
// */
//@Entity
//@Table(name = "refresh_token")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class RefreshToken {
//
//    // 토큰 번호
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "token_id")
//    private Long tokenId;
//
//    // 토큰 주인 (회원)
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "member_id", nullable = false)
//    private User member;
//
//    // 토큰 원문의 SHA-256 해시 (원문 저장 금지)
//    @Column(name = "token_hash", nullable = false, length = 255)
//    private String tokenHash;
//
//    // 만료일시. 지나면 재발급에 쓸 수 없다
//    @Column(name = "expires_at", nullable = false)
//    private LocalDateTime expiresAt;
//
//    // 등록일시
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    // 폐기일시. 로그아웃하거나 재발급에 한 번 쓰면 값이 들어간다
//    @Column(name = "revoked_at")
//    private LocalDateTime revokedAt;
//
//    public RefreshToken(User member, String tokenHash, LocalDateTime expiresAt, LocalDateTime now) {
//        this.member = member;
//        this.tokenHash = tokenHash;
//        this.expiresAt = expiresAt;
//        this.createdAt = now;
//    }
//
//    // 아직 쓸 수 있는 토큰인지 (폐기되지 않았고 만료 전)
//    public boolean isUsable(LocalDateTime now) {
//        return revokedAt == null && expiresAt.isAfter(now);
//    }
//
//    // 토큰 폐기. 이미 폐기된 토큰은 처음 폐기 시각을 그대로 둔다
//    public void revoke(LocalDateTime now) {
//        if (revokedAt == null) {
//            revokedAt = now;
//        }
//    }
//}
