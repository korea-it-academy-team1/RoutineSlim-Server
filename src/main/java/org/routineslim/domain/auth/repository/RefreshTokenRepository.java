package org.routineslim.domain.auth.repository;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

//import org.routineslim.domain.auth.entity.RefreshToken;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//// [JWT 로그인 - 이대수] refresh token 저장소
//public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
//
//    // 앱/웹이 보낸 토큰 원문을 해시한 값으로 저장된 토큰을 찾는다
//    Optional<RefreshToken> findByTokenHash(String tokenHash);
//}
