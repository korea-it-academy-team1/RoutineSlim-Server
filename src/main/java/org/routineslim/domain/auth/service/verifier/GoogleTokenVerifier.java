package org.routineslim.domain.auth.service.verifier;

import org.routineslim.domain.auth.dto.SocialUserInfoDTO;
import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private final RestClient restClient = RestClient.create();

    public SocialUserInfoDTO verify(String accessToken) {

        // 구글 사용자 정보 API 호출
        Map<String, Object> response = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        // 구글 사용자 정보 확인
        if (response == null || response.get("sub") == null) {
            throw new IllegalArgumentException(
                    "구글 사용자 정보를 가져올 수 없습니다."
            );
        }

        // 구글 사용자 고유 ID
        String providerUserId = response.get("sub").toString();

        if (providerUserId.isBlank()) {
            throw new IllegalArgumentException(
                    "구글 사용자 ID가 없습니다."
            );
        }

        // 이름과 이메일 가져오기
        String name = (String) response.get("name");
        String email = (String) response.get("email");

        // 구글 이메일 인증 여부 확인
        if (email != null
                && !Boolean.TRUE.equals(response.get("email_verified"))) {
            throw new IllegalArgumentException(
                    "인증되지 않은 구글 이메일입니다."
            );
        }
        if (name == null || name.isBlank()) {
            name = "소셜회원";
        }

        // 공통 DTO 반환
        return SocialUserInfoDTO.builder()
                .provider(SocialProvider.GOOGLE)
                .providerUserId(providerUserId)
                .name(name)
                .email(email)
                .build();
    }
}
