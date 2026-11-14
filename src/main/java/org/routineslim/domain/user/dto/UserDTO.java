package org.routineslim.domain.user.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    // 회원 식별자
    private UUID memberId;

    // 아이디
    private String memberName;

    // 이름
    private String name;

    // 이메일
    private String email;

    // 전화번호
    private String phone;

    // 기본 알림 일수
    private Integer defaultNotificationDays;

    // 알림 시간
    private LocalTime notificationTime;

    // 가입일
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;
}
