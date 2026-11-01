package org.routineslim.domain.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {

    // 아이디
    private String memberName;

    // 비밀번호
    private String memberPassword;

    // 이름
    private String name;

    // 이메일
    private String email;

    // 전화번호
    private String phone;
}
