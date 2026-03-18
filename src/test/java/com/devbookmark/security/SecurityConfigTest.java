package com.devbookmark.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;

    // ─── Public endpoints — no token needed ───────────────────────────────────

    @Test
    void actuatorHealth_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void authRegister_isPublic() throws Exception {
        // Empty body → 400 (validation), NOT 401 — proves endpoint is reachable
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authLogin_isPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exploreEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/api/explore/trending"))
                .andExpect(status().isOk());
    }

    @Test
    void resourcesGet_isPublic() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk());
    }

    @Test
    void searchEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/api/search?q=java"))
                .andExpect(status().isOk());
    }

    @Test
    void tagsGet_isPublic() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk());
    }

    // ─── Protected endpoints — 401 without token ──────────────────────────────

    @Test
    void feed_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void notifications_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingList_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/reading-list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recommendations_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/recommendations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpoint_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Actuator — non-health endpoints require auth ─────────────────────────

    @Test
    void actuatorEnv_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }
}