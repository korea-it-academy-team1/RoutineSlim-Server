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

    // 토큰
    private String token;
}
