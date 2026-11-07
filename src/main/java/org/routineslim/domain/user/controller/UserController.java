package org.routineslim.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.user.dto.UserDTO;
import org.routineslim.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원정보 조회
    @GetMapping("/getMyInfo")
    public UserDTO getMyInfo (@AuthenticationPrincipal UUID memberId) {
        return userService.getMyInfo(memberId);
    }

    // 회원정보 수정
    @PutMapping("/updateMyInfo")
    public void  updateMyInfo (@AuthenticationPrincipal UUID memberId,
                              @RequestBody UserDTO userDTO){
        userService.updateMyInfo(memberId, userDTO);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdrawUser")
    public void withdrawUser (@AuthenticationPrincipal UUID memberId){
        userService.withdrawUser(memberId);
    }
}
