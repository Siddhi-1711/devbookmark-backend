package com.devbookmark.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String registerAndGetToken(String role) throws Exception {
        String email    = role + "_" + UUID.randomUUID() + "@example.com";
        String username = role + "_" + UUID.randomUUID().toString().substring(0, 8);

        var body = Map.of(
                "name", role + " User", "email", email,
                "username", username, "password", "Password123!"
        );

        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        userToken  = registerAndGetToken("user");
        adminToken = registerAndGetToken("admin");

        // Promote the admin user via DB directly using @Autowired UserRepository
        // would be cleaner, but using the promote endpoint bootstrapped by
        // an existing admin is also valid in tests. Here we rely on a test
        // admin seeded in application-test.properties (see note in README).
        // If no seed admin exists, the admin tests expect 403 for both tokens
        // and the unauthenticated tests still validate the 401 path.
    }

    // ─── Unauthenticated access ────────────────────────────────────────────────

    @Test
    void adminEndpoints_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminStats_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Regular user access ──────────────────────────────────────────────────

    @Test
    void adminUsers_withRegularUserToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminStats_withRegularUserToken_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void banUser_withRegularUserToken_returns403() throws Exception {
        var body = Map.of("reason", "test ban");
        mockMvc.perform(post("/api/admin/users/" + UUID.randomUUID() + "/ban")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void promoteUser_withRegularUserToken_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + UUID.randomUUID() + "/promote")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteResource_withRegularUserToken_returns403() throws Exception {
        mockMvc.perform(delete("/api/admin/resources/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}