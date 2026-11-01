package org.routineslim.domain.auth.service.verifier;

import org.routineslim.domain.auth.dto.SocialUserInfoDTO;
import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class NaverTokenVerifier {

    private final RestClient restClient = RestClient.create();

    public SocialUserInfoDTO verify(String accessToken) {

        // 네이버 사용자 정보 API 호출
        Map<String, Object> result = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        // 응답 데이터 확인
        if (result == null || !"00".equals(result.get("resultcode"))) {
            throw new IllegalArgumentException(
                    "네이버 사용자 정보를 가져올 수 없습니다."
            );
        }

        // 사용자 정보 가져오기
        Object response = result.get("response");

        if (!(response instanceof Map<?, ?> userInfo)) {
            throw new IllegalArgumentException(
                    "네이버 사용자 정보가 없습니다."
            );
        }

        // 네이버 사용자 고유 ID
        Object id = userInfo.get("id");

        if (id == null || id.toString().isBlank()) {
            throw new IllegalArgumentException(
                    "네이버 사용자 ID가 없습니다."
            );
        }

        String providerUserId = id.toString();

        // 이름과 이메일 가져오기
        String name = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");

        if (name == null || name.isBlank()) {
            name = "소셜회원";
        }

        // 공통 DTO 반환
        return SocialUserInfoDTO.builder()
                .provider(SocialProvider.NAVER)
                .providerUserId(providerUserId)
                .name(name)
                .email(email)
                .build();
    }
}
