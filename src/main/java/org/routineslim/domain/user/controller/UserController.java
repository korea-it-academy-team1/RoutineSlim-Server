package org.routineslim.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.user.dto.NotificationSettingsRequestDTO;
import org.routineslim.domain.user.dto.UserDTO;
import org.routineslim.domain.user.dto.UserResponseDTO;
import org.routineslim.domain.user.service.UserService;
import org.springframework.web.bind.annotation.*;

//// [JWT 로그인 - 이대수] access token 에서 회원 식별자를 꺼낼 때 쓰는 import
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/*
 * [로그인 - 이대수] 지금은 1번 방식: 앱/웹이 로그인 때 받은 회원 UUID 를 헤더 "X-Member-Id: <UUID>" 로 보낸다 (없으면 400)
 * JWT 로 바꿀 때는 각 메서드의 @RequestHeader("X-Member-Id") UUID memberId 를
 * @AuthenticationPrincipal Jwt jwt 로 바꾸고, memberId 는 UUID.fromString(jwt.getSubject()) 로 꺼낸다
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원정보 조회
    @GetMapping("/getMyInfo")
    public UserDTO getMyInfo(@RequestHeader("X-Member-Id") UUID memberId) {
        return userService.getMyInfo(memberId);
    }

    // 회원정보 수정
    @PutMapping("/updateMyInfo")
    public void updateMyInfo(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestBody UserDTO userDTO) {

        userService.updateMyInfo(memberId, userDTO);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdrawUser")
    public void withdrawUser(@RequestHeader("X-Member-Id") UUID memberId) {
        userService.withdrawUser(memberId);
    }

    // [1번 임시 - 이대수] 로그인한 회원 정보 (헤더 X-Member-Id)
    @GetMapping("/me")
    public UserResponseDTO me(@RequestHeader("X-Member-Id") UUID memberId) {
        return userService.getUser(memberId);
    }

//    // [JWT 로그인 - 이대수] 로그인한 회원 정보
//    // 헤더 "Authorization: Bearer {accessToken}" 이 있어야 호출된다 (없으면 SecurityConfig 가 401)
//    @GetMapping("/me")
//    public UserResponseDTO me(@AuthenticationPrincipal Jwt jwt) {
//        return userService.getUser(UUID.fromString(jwt.getSubject()));
//    }

    // 알림수정
    @PutMapping("/notification-settings")
    public void updateNotificationSettings(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestBody NotificationSettingsRequestDTO notificationSettingsRequestDTO) {

        // Service에 알림 설정 수정 요청
        userService.updateNotificationSettings(memberId, notificationSettingsRequestDTO);
    }
}
