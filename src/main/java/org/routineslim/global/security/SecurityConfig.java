package org.routineslim.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/*
 * [로그인 - 이대수] 지금은 "1번 방식"(로그인하면 회원 UUID 를 돌려주고, 앱/웹이 X-Member-Id 헤더로 보냄)이다.
 * 나중에 JWT(3번)로 바꿀 때: [1번 임시 - 이대수] 줄은 주석 처리(또는 삭제),
 * [JWT 로그인 - 이대수] 줄은 주석 해제 (IntelliJ: 줄 선택 후 Ctrl + /). 전체 목록은 docs/jwt-switch-guide.md
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // [로그인 - 이대수] 웹(브라우저)에서 다른 포트의 서버를 부를 수 있게 CORS 허용 (아래 corsConfigurationSource)
                .cors(Customizer.withDefaults())

                // [로그인 - 이대수] 로그인 상태를 서버 세션으로 기억하지 않는다 (1번: 헤더, 3번: 토큰)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // [로그인 - 이대수] 기본 로그인 폼·브라우저 팝업 로그인은 쓰지 않는다
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/social/login",
//                                // [JWT 로그인 - 이대수] 토큰 재발급·로그아웃은 access token 이 만료된 상태에서도 불러야 하므로 연다
//                                "/auth/refresh",
//                                "/auth/logout",
                                // [로그인 - 이대수] 오류 응답(401, 404 등)이 Security 에 막혀 내용 없이 오는 것을 막는다
                                "/error",
                                // [로컬 개발 - 이대수] H2 DB 콘솔. spring.h2.console.enabled=true 인 local 에서만 실제로 열린다
                                "/h2-console/**").permitAll()

                        // [1번 임시 - 이대수] 토큰 검사가 없으므로 나머지 API 도 모두 연다
                        // (회원 구분은 각 API 가 X-Member-Id 헤더로 한다. 보안 없음 → 시연용)
                        .anyRequest().permitAll()

//                        // [JWT 로그인 - 이대수] 나머지 API 는 로그인(유효한 access token)이 있어야 호출된다
//                        .anyRequest().authenticated()
                )

                // [로컬 개발 - 이대수] H2 콘솔은 화면을 iframe 으로 그리므로 같은 주소의 iframe 은 허용한다
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()));

//        // [JWT 로그인 - 이대수] "Authorization: Bearer <accessToken>" 헤더를 JwtConfig 의 JwtDecoder 로 검사한다
//        // 토큰이 없거나, 서명이 틀리거나, 만료되면 401 을 돌려준다
//        http.oauth2ResourceServer(resourceServer -> resourceServer
//                .jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * [로그인 - 이대수] CORS 설정.
     * 브라우저는 다른 주소(포트)의 서버를 부를 때 허용 여부를 먼저 묻는다.
     * 안드로이드 네이티브 앱 요청에는 CORS 가 적용되지 않으므로 웹 개발 주소만 넣으면 된다.
     * 웹을 배포하면 그 도메인을 여기에 추가한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:8081",   // Expo 웹 개발 서버 (RoutineSlim-App)
                "http://localhost:5173"    // Vite 개발 서버 (RoutineSlim-Web)
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // [1번 임시 - 이대수] 회원 UUID 를 보내는 X-Member-Id 헤더 허용
        config.setAllowedHeaders(List.of("Content-Type", "X-Member-Id"));
//        // [JWT 로그인 - 이대수] 토큰을 보내는 Authorization 헤더 허용
//        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
