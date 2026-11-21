package org.routineslim.domain.cleanup;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 정리 목록 API (/cleanup) 테스트. V3 의 admin 계정, 메모리 H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanupControllerTest {

    private static final String ADMIN_ID = "019a3c2e-0000-7000-8000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    // 연간 120,000원 구독 → 월 환산 10,000원
    private String createYearlySubscription() throws Exception {
        String body = mockMvc.perform(post("/api/v1/subscriptions").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "테스트 연간", "categoryId": 1, "amount": 120000,
                                 "billingCycle": "YEARLY", "startDate": "2026-01-10"}
                                """))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String saveBody(String subscriptionId, String action) {
        return """
                {"items": [{"subscriptionId": "%s", "action": "%s"}]}
                """.formatted(subscriptionId, action);
    }

    @Test
    void 해지로_담으면_월_환산_금액과_알림일이_저장된다() throws Exception {
        String id = createYearlySubscription();

        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "CANCEL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].subscriptionId").value(id))
                .andExpect(jsonPath("$[0].action").value("CANCEL"))
                .andExpect(jsonPath("$[0].monthlyAmount").value(10000))
                .andExpect(jsonPath("$[0].newMonthlyAmount").value(0))
                .andExpect(jsonPath("$[0].remindOn").isNotEmpty());

        mockMvc.perform(get("/cleanup").header("X-Member-Id", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void 다시_담으면_덮어쓰고_KEEP_이면_뺀다() throws Exception {
        String id = createYearlySubscription();

        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "CANCEL")));
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"subscriptionId": "%s", "action": "DOWNGRADE", "newMonthlyAmount": 5500}]}
                                """.formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].action").value("DOWNGRADE"))
                .andExpect(jsonPath("$[0].newMonthlyAmount").value(5500));

        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "KEEP")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void 해지한_구독은_목록에서_빠진다() throws Exception {
        String id = createYearlySubscription();
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "CANCEL")));

        mockMvc.perform(patch("/api/v1/subscriptions/" + id + "/status").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"CANCELED\"}"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/cleanup").header("X-Member-Id", ADMIN_ID))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void 잘못된_요청은_400() throws Exception {
        String id = createYearlySubscription();

        // 정리 방법 오타
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "DELETE")))
                .andExpect(status().isBadRequest());

        // 요금제 변경인데 금액 없음
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "DOWNGRADE")))
                .andExpect(status().isBadRequest());

        // 다른 회원의 구독(없는 구독)
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveBody("019a3c2e-0000-7000-8000-00000000ffff", "CANCEL")))
                .andExpect(status().isBadRequest());

        // 빈 목록
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"items\": []}"))
                .andExpect(status().isBadRequest());

        // 담지 않은 구독 빼기
        mockMvc.perform(delete("/cleanup/" + id).header("X-Member-Id", ADMIN_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 정리_목록에서_뺀다() throws Exception {
        String id = createYearlySubscription();
        mockMvc.perform(post("/cleanup").header("X-Member-Id", ADMIN_ID)
                .contentType(MediaType.APPLICATION_JSON).content(saveBody(id, "CANCEL")));

        mockMvc.perform(delete("/cleanup/" + id).header("X-Member-Id", ADMIN_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/cleanup").header("X-Member-Id", ADMIN_ID))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
