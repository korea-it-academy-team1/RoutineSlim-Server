package org.routineslim.domain.auth.service.verifier;

import org.routineslim.domain.auth.dto.SocialUserInfoDTO;
import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class KakaoTokenVerifier {

    private final RestClient restClient = RestClient.create();

    public SocialUserInfoDTO verify(String accessToken){

        // 카카오 사용자 정보 API 호출
        Map<String, Object> response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        // 카카오 사용자 ID 확인
        if (response == null || response.get("id") == null) {
            throw new IllegalArgumentException(
                    "카카오 사용자 정보를 가져올 수 없습니다."
            );
        }

        // 카카오 사용자 고유 ID
        String providerUserId = response.get("id").toString();

        // 카카오 계정 정보
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) response.get("kakao_account");

        String email = null;
        String name = "소셜회원";

        // 이메일과 닉네임 가져오기
        if (kakaoAccount != null) {

            email = (String) kakaoAccount.get("email");

            Map<String, Object> profile =
                    (Map<String, Object>) kakaoAccount.get("profile");

            if (profile != null && profile.get("nickname") != null) {
                name = profile.get("nickname").toString();
            }
        }

        // 공통 DTO 반환
        return SocialUserInfoDTO.builder()
                .provider(SocialProvider.KAKAO)
                .providerUserId(providerUserId)
                .name(name)
                .email(email)
                .build();

    }
}
