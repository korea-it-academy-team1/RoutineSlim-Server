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

//    // [JWT 로그인 - 이대수] 로그인 상태 유지 여부 (로그인 화면 체크박스)
//    // true 면 refresh token 14일, false(또는 생략)면 1일 (application.yml 의 jwt.* 값)
//    // (1번 방식에서는 쓰지 않는다. 앱이 이 값을 보내도 모르는 필드라 무시된다)
    private boolean keepLoggedIn;
}
