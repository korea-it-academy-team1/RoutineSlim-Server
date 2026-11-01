package org.routineslim.domain.user.dto;

import lombok.*;
import org.routineslim.domain.user.entity.User;

import java.util.UUID;

/* [로그인 - 이대수] 내 정보 응답 (GET /api/v1/users/me). 비밀번호는 절대 내보내지 않는다 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    // 회원 식별자
    private UUID memberId;

    // 아이디
    private String memberName;

    // 이름
    private String name;

    // 이메일
    private String email;

    // 엔티티 → 응답 DTO 변환
    public static UserResponseDTO from(User user) {
        return UserResponseDTO.builder()
                .memberId(user.getMemberId())
                .memberName(user.getMemberName())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
