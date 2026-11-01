package org.routineslim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.auth.dto.LoginRequestDTO;
import org.routineslim.domain.auth.dto.SignupRequestDTO;
import org.routineslim.domain.auth.repository.SocialAccountRepository;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.routineslim.global.util.UuidV7Generator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final SocialAccountRepository socialAccountRepository;

    // 회원가입
    @Override
    public UUID signup(SignupRequestDTO signupRequestDTO) {

        // 아이디 중복 확인
        if (userRepository.existsByMemberName(signupRequestDTO.getMemberName())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 해시처리
        String encodedPassword =
                passwordEncoder.encode(signupRequestDTO.getMemberPassword());

        // 회원 객체 생성 (DTO 데이터를 Member 엔티티로 변환)
        User member = User.builder()
                .memberId(UuidV7Generator.generate())
                .memberName(signupRequestDTO.getMemberName())
                .memberPassword(encodedPassword)
                .name(signupRequestDTO.getName())
                .email(signupRequestDTO.getEmail())
                .phone(signupRequestDTO.getPhone())
                .build();

        // 회원 저장
        User saveUser = userRepository.save(member);

        // 5. 저장된 회원번호 반환
        return saveUser.getMemberId();
    }

    // 로그인
    @Override
    public UUID login(LoginRequestDTO loginRequestDTO) {

        // 아이디로 회원 조회
        User user = userRepository.findByMemberName(
                loginRequestDTO.getMemberName()
        ).orElseThrow(() ->
                new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.")
        );

        // 소셜 계정은 일반 로그인 사용 불가
        if (socialAccountRepository.existsByMember(user)) {
            throw new IllegalArgumentException("소셜 로그인으로 가입한 계정입니다. 소셜 로그인을 이용해 주세요.");
        }

        // 입력한 비밀번호 db에서 확인
        if (!passwordEncoder.matches(
                loginRequestDTO.getMemberPassword(),
                user.getMemberPassword()
        )) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 회원번호 반환
        return user.getMemberId();
    }
}
