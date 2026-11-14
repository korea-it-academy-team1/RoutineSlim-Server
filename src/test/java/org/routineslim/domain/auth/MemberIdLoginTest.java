package org.routineslim.domain.auth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [1번 임시 - 이대수] 1번 방식(회원 UUID + X-Member-Id 헤더) 로그인 흐름 테스트.
 * JWT(3번)로 바꾸면 이 파일은 지우고, 주석 처리된 AuthControllerTest 를 되살린다.
 * V3__insert_admin_member.sql 의 admin 계정을 쓴다. DB 는 application-test.yml 의 메모리 H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberIdLoginTest {

    // V3__insert_admin_member.sql 의 고정 UUID
    private static final String ADMIN_ID = "019a3c2e-0000-7000-8000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 로그인하면_회원_UUID_를_돌려준다() throws Exception {
        String body = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberName": "admin", "memberPassword": "qwert123!@", "keepLoggedIn": true}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 응답 본문은 JSON 문자열 "019a..." (따옴표 포함)
        org.junit.jupiter.api.Assertions.assertEquals(ADMIN_ID, JsonPath.read(body, "$"));
    }

    @Test
    void X_Member_Id_헤더로_내_정보를_조회한다() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("X-Member-Id", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberName").value("admin"))
                .andExpect(jsonPath("$.name").value("관리자"));
    }

    @Test
    void 헤더가_없으면_400() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 없는_회원이면_404() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("X-Member-Id", "00000000-0000-7000-8000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
