package org.routineslim.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.routineslim.domain.user.dto.UserResponseDTO;
import org.routineslim.domain.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

//// [JWT 로그인 - 이대수] access token 에서 회원 식별자를 꺼낼 때 쓰는 import
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    // [로그인 - 이대수] 회원 정보 조회
    private final UserService userService;

    @GetMapping
    public void user(){}

    // [1번 임시 - 이대수] 로그인한 회원 정보
    // 앱/웹이 로그인 때 받은 회원 UUID 를 헤더 "X-Member-Id: <UUID>" 로 보낸다 (없으면 400)
    // 다른 API 도 로그인한 회원이 필요하면 같은 방식으로 @RequestHeader("X-Member-Id") UUID memberId 를 받는다
    // 주의: 헤더만 바꾸면 다른 회원 행세를 할 수 있다 (보안 없음 → 시연용)
    @GetMapping("/me")
    public UserResponseDTO me(@RequestHeader("X-Member-Id") UUID memberId) {
        return userService.getUser(memberId);
    }

//    // [JWT 로그인 - 이대수] 로그인한 회원 정보
//    // 헤더 "Authorization: Bearer {accessToken}" 이 있어야 호출된다 (없으면 SecurityConfig 가 401)
//    // @AuthenticationPrincipal Jwt : Spring Security 가 검증을 마친 access token 내용
//    // jwt.getSubject() : 토큰 발급 때 넣은 회원 식별자 (JwtTokenProvider 참고)
//    @GetMapping("/me")
//    public UserResponseDTO me(@AuthenticationPrincipal Jwt jwt) {
//        return userService.getUser(UUID.fromString(jwt.getSubject()));
//    }
}
