package org.routineslim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.auth.dto.SocialLoginRequestDTO;
import org.routineslim.domain.auth.dto.SocialUserInfoDTO;
import org.routineslim.domain.auth.entity.SocialAccount;
import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.routineslim.domain.auth.repository.SocialAccountRepository;
import org.routineslim.domain.auth.service.verifier.GoogleTokenVerifier;
import org.routineslim.domain.auth.service.verifier.KakaoTokenVerifier;
import org.routineslim.domain.auth.service.verifier.NaverTokenVerifier;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.routineslim.global.util.UuidV7Generator;
import org.routineslim.domain.user.entity.User;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class SocialLoginServiceImpl implements SocialLoginService{

    private final SocialAccountRepository socialAccountRepository;

    private final GoogleTokenVerifier googleTokenVerifier;
    private final KakaoTokenVerifier kakaoTokenVerifier;
    private final NaverTokenVerifier naverTokenVerifier;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UUID socialLogin(SocialLoginRequestDTO socialLoginRequestDTO) {

        // 소셜 로그인 제공업체 가져오기
        SocialProvider provider = socialLoginRequestDTO.getProvider();

        // 프론트엔드에서 전달한 토큰 가져오기
        String token = socialLoginRequestDTO.getToken();

        // 제공업체 구분
        if (provider == null) {
            throw new IllegalArgumentException("소셜 로그인 제공업체가 없습니다.");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("소셜 로그인 토큰이 없습니다.");
        }

        // 업체별 토큰 검증 후 사용자 ID 가져오기
        SocialUserInfoDTO userInfo = switch (provider) {

            case GOOGLE -> googleTokenVerifier.verify(token);

            case KAKAO -> kakaoTokenVerifier.verify(token);

            case NAVER -> naverTokenVerifier.verify(token);
        };

        // 검증된 사용자 ID 가져오기
        String providerUserId = userInfo.getProviderUserId();

        // 소셜 사용자 ID 확인
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("소셜 사용자 ID가 없습니다.");
        }

        // 소셜 회원 이름 확인
        String name = userInfo.getName();

        if (name == null || name.isBlank()) {
            name = "소셜회원";
        }

        // 공통 회원 조회
        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);

        // 기존 회원이면 회원 ID 반환
        if (socialAccount != null) {
            return socialAccount.getMember().getMemberId();
        }

        // 소셜 회원 이메일 가져오기
        String email = userInfo.getEmail();

        // 이메일 중복 검사
        if (email != null && !email.isBlank()
                && userRepository.existsByEmail(email)) {

            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 신규 소셜 회원 아이디 자동 생성
        String generatedMemberName =
                "social_" + UUID.randomUUID().toString().replace("-", "");

        // 임의 비밀번호 생성
        String randomPassword = UUID.randomUUID().toString();

        // 비밀번호 해시 처리
        String encodedPassword = passwordEncoder.encode(randomPassword);

        // 신규 소셜 회원 객체 생성
        User user = User.builder()
                .memberId(UuidV7Generator.generate())
                .memberName(generatedMemberName)
                .memberPassword(encodedPassword)
                .name(name)
                .email(email)
                .build();

        // 신규 회원 DB 저장
        userRepository.save(user);

        // 신규 회원과 소셜 계정 연결
        SocialAccount newSocialAccount = SocialAccount.builder()
                .member(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();

        // 신규 소셜 계정 DB 저장
        socialAccountRepository.save(newSocialAccount);

        // 신규 회원 ID 반환
        return user.getMemberId();
    }
}
