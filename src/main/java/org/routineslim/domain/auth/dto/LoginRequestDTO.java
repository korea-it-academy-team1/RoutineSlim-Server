package org.routineslim.domain.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    // 아이디
    private String memberName;

    // 비밀번호
    private String memberPassword;
}
