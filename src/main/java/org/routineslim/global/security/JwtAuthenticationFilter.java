package org.routineslim.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.routineslim.domain.user.repository.UserRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더 가져오기
        String authorization = request.getHeader("Authorization");

        // 2. Bearer 토큰인지 확인
        if (authorization != null &&
                authorization.startsWith("Bearer ")) {

            String token = authorization.substring(7);

            // 3. JWT 유효성 검사
            if (jwtTokenProvider.validateToken(token)) {

                // 4. JWT에서 회원 UUID 추출
                try {
                    UUID memberId = jwtTokenProvider.getMemberId(token);

                    // 5. 탈퇴하지 않은 회원인지 확인
                    if (userRepository
                            .findByMemberIdAndDeletedAtIsNull(memberId)
                            .isPresent()) {

                        // 6. Spring Security 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        memberId,
                                        null,
                                        List.of()
                                );

                        // 7. 인증 정보 등록
                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }

                } catch (IllegalArgumentException e) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        // 8. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
