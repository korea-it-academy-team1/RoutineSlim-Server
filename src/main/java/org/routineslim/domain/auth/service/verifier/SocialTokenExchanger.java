package org.routineslim.domain.auth.service.verifier;

import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * [소셜 로그인 - 권기쁨] 카카오·네이버 인가 코드 → access token 교환.
 *
 * 앱은 로그인 창에서 받은 인가 코드(code)만 서버에 보내고, 서버가 Client Secret 으로 토큰을 받는다.
 * (Secret 을 앱에 넣으면 누구나 꺼내 볼 수 있어서 서버에서만 쓴다)
 * 받은 access token 은 기존 KakaoTokenVerifier / NaverTokenVerifier 가 그대로 검증한다.
 *
 * 설정 값 (application.yml 의 social.*, 환경 변수 또는 application-local.yml 로 넣는다)
 *   social.kakao.client-id      카카오 REST API 키
 *   social.kakao.client-secret  카카오 Client Secret (콘솔에서 "사용함"일 때만 필요, 아니면 비워 둔다)
 *   social.naver.client-id      네이버 Client ID
 *   social.naver.client-secret  네이버 Client Secret
 */
@Component
public class SocialTokenExchanger {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";

    private final RestClient restClient = RestClient.create();

    private final String kakaoClientId;
    private final String kakaoClientSecret;
    private final String naverClientId;
    private final String naverClientSecret;

    public SocialTokenExchanger(
            @Value("${social.kakao.client-id:}") String kakaoClientId,
            @Value("${social.kakao.client-secret:}") String kakaoClientSecret,
            @Value("${social.naver.client-id:}") String naverClientId,
            @Value("${social.naver.client-secret:}") String naverClientSecret) {
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
        this.naverClientId = naverClientId;
        this.naverClientSecret = naverClientSecret;
    }

    /** 인가 코드를 access token 으로 바꾼다. 실패하면 IllegalArgumentException (→ 400) */
    public String exchange(SocialProvider provider, String code, String redirectUri, String state) {
        return switch (provider) {
            case KAKAO -> exchangeKakao(code, redirectUri);
            case NAVER -> exchangeNaver(code, state);
            case GOOGLE -> throw new IllegalArgumentException("구글 로그인은 인가 코드가 아니라 token 으로 보내야 합니다.");
        };
    }

    private String exchangeKakao(String code, String redirectUri) {
        if (kakaoClientId.isBlank()) {
            throw new IllegalArgumentException("서버에 카카오 로그인 설정(social.kakao.client-id)이 없습니다.");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalArgumentException("카카오 로그인에는 redirectUri 가 필요합니다.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoClientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);
        if (!kakaoClientSecret.isBlank()) {
            form.add("client_secret", kakaoClientSecret);
        }

        return requestAccessToken(KAKAO_TOKEN_URL, form, "카카오");
    }

    private String exchangeNaver(String code, String state) {
        if (naverClientId.isBlank() || naverClientSecret.isBlank()) {
            throw new IllegalArgumentException("서버에 네이버 로그인 설정(social.naver.client-id / client-secret)이 없습니다.");
        }
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("네이버 로그인에는 state 가 필요합니다.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", naverClientId);
        form.add("client_secret", naverClientSecret);
        form.add("code", code);
        form.add("state", state);

        return requestAccessToken(NAVER_TOKEN_URL, form, "네이버");
    }

    @SuppressWarnings("unchecked")
    private String requestAccessToken(String url, MultiValueMap<String, String> form, String providerName) {
        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            // 코드 만료·재사용, redirect URI 불일치, 키 오류 등 → 400
            throw new IllegalArgumentException(providerName + " 로그인 코드를 확인하지 못했습니다. 다시 로그인해 주세요.");
        }

        // 네이버는 실패해도 200 으로 error 필드를 준다
        Object accessToken = response == null ? null : response.get("access_token");
        if (accessToken == null || accessToken.toString().isBlank()) {
            throw new IllegalArgumentException(providerName + " 로그인 코드를 확인하지 못했습니다. 다시 로그인해 주세요.");
        }
        return accessToken.toString();
    }
}
