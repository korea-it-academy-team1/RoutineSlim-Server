package org.routineslim.domain.auth.dto;

import lombok.*;
import org.routineslim.domain.auth.entity.enums.SocialProvider;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialUserInfoDTO {

    // 소셜 로그인 제공업체
    private SocialProvider provider;

    // 소셜 제공업체에서 발급한 사용자 ID
    private String providerUserId;

    // 사용자 이름
    private String name;

    // 사용자 이메일
    private String email;


}
