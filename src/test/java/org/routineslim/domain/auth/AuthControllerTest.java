package org.routineslim.domain.auth;

/*
 * [JWT 로그인 - 이대수] 3번(JWT) 방식용 파일. 지금은 1번 방식이라 전체를 주석 처리해 두었다.
 * JWT 로 바꿀 때: 아래 //import 줄부터 파일 끝까지 선택 → Ctrl + / (주석 해제)
 * 전체 순서는 docs/jwt-switch-guide.md
 */

//import com.jayway.jsonpath.JsonPath;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * [JWT 로그인 - 이대수] 로그인 → 토큰으로 API 호출 → 재발급 → 로그아웃 흐름 테스트.
// * V3__insert_admin_member.sql 의 admin 계정을 쓴다. DB 는 application-test.yml 의 메모리 H2.
// * 각 테스트는 @Transactional 이라 끝나면 저장한 refresh token 이 되돌려진다.
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//class AuthControllerTest {
//
//    private static final String ADMIN_LOGIN = """
//            {"memberName": "admin", "memberPassword": "qwert123!@", "keepLoggedIn": true}
//            """;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void 로그인하면_토큰으로_내_정보를_조회할_수_있다() throws Exception {
//        String accessToken = JsonPath.read(login(ADMIN_LOGIN), "$.accessToken");
//
//        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.memberName").value("admin"))
//                .andExpect(jsonPath("$.name").value("관리자"));
//    }
//
//    @Test
//    void 토큰_없이_API_를_부르면_401() throws Exception {
//        mockMvc.perform(get("/api/v1/users/me"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void 위조된_토큰으로_API_를_부르면_401() throws Exception {
//        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer not-a-real-token"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void 재발급하면_예전_refresh_token_은_다시_쓸_수_없다() throws Exception {
//        String oldRefreshToken = JsonPath.read(login(ADMIN_LOGIN), "$.refreshToken");
//
//        // 처음 한 번은 성공
//        refresh(oldRefreshToken)
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").isNotEmpty());
//
//        // 같은 토큰을 다시 쓰면 실패 (rotation)
//        refresh(oldRefreshToken)
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void 로그아웃한_refresh_token_은_쓸_수_없다() throws Exception {
//        String refreshToken = JsonPath.read(login(ADMIN_LOGIN), "$.refreshToken");
//
//        mockMvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON)
//                        .content(refreshBody(refreshToken)))
//                .andExpect(status().isOk());
//
//        refresh(refreshToken).andExpect(status().isUnauthorized());
//    }
//
//    // 로그인하고 응답 본문(JSON 문자열)을 돌려준다
//    private String login(String body) throws Exception {
//        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//    }
//
//    private ResultActions refresh(String refreshToken) throws Exception {
//        return mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
//                .content(refreshBody(refreshToken)));
//    }
//
//    private static String refreshBody(String refreshToken) {
//        return "{\"refreshToken\": \"" + refreshToken + "\"}";
//    }
//}
