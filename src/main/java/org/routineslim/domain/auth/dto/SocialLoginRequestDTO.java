package org.routineslim.domain.auth.dto;

import lombok.*;
import org.routineslim.domain.auth.entity.enums.SocialProvider;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLoginRequestDTO {

    // 소셜 로그인 제공업체
    private SocialProvider provider;

    // 토큰 (구글: 앱이 받은 access token)
    private String token;

    // [소셜 로그인 - 권기쁨] 카카오·네이버는 token 대신 인가 코드를 보낸다.
    // 토큰 교환에 Client Secret 이 필요해서 앱이 아니라 서버가 교환한다 (SocialTokenExchanger)
    // 인가 코드
    private String code;

    // 인가 요청 때 쓴 redirect URI (카카오 토큰 교환에 같은 값이 필요)
    private String redirectUri;

    // 인가 요청 때 쓴 state (네이버 토큰 교환에 필요)
    private String state;
}
