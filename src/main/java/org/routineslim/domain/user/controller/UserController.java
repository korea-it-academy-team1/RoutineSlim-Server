package org.routineslim.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.user.dto.UserDTO;
import org.routineslim.domain.user.dto.UserResponseDTO;
import org.routineslim.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// [JWT 로그인 - 이대수] access token 에서 회원 식별자를 꺼낼 때 쓰는 import
import org.springframework.security.oauth2.jwt.Jwt;


import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원정보 조회
    @GetMapping("/getMyInfo")
    public UserDTO getMyInfo(@AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        return userService.getMyInfo(memberId);
    }

    // 회원정보 수정
    @PutMapping("/updateMyInfo")
    public void updateMyInfo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserDTO userDTO) {

        UUID memberId = UUID.fromString(jwt.getSubject());
        userService.updateMyInfo(memberId, userDTO);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdrawUser")
    public void withdrawUser(@AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        userService.withdrawUser(memberId);
    }

//    // JWT 기반 회원정보 조회
//    @GetMapping("/me")
//    public UserResponseDTO me(@RequestHeader("X-Member-Id") UUID memberId) {
//        return userService.getUser(memberId);
//    }

    // [JWT 로그인 - 이대수] 로그인한 회원 정보
    // 헤더 "Authorization: Bearer {accessToken}" 이 있어야 호출된다 (없으면 SecurityConfig 가 401)
    // @AuthenticationPrincipal Jwt : Spring Security 가 검증을 마친 access token 내용
    // jwt.getSubject() : 토큰 발급 때 넣은 회원 식별자 (JwtTokenProvider 참고)
    @GetMapping("/me")
    public UserResponseDTO me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getUser(UUID.fromString(jwt.getSubject()));
    }
}
