package com.devbookmark.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ─── Register ─────────────────────────────────────────────────────────────

    @Test
    void register_withValidData_returns200AndToken() throws Exception {
        var body = Map.of(
                "name",     "Test User",
                "email",    "test_" + UUID.randomUUID() + "@example.com",
                "username", "testuser_" + UUID.randomUUID().toString().substring(0, 8),
                "password", "Password123!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").isNotEmpty());
    }

    @Test
    void register_withDuplicateEmail_returns400() throws Exception {
        String email = "dupe_" + UUID.randomUUID() + "@example.com";
        String username1 = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String username2 = "user_" + UUID.randomUUID().toString().substring(0, 8);

        var body1 = Map.of(
                "name", "User One", "email", email,
                "username", username1, "password", "Password123!"
        );
        var body2 = Map.of(
                "name", "User Two", "email", email,
                "username", username2, "password", "Password123!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void register_withMissingFields_returns400() throws Exception {
        // missing password
        var body = Map.of(
                "name",  "Test User",
                "email", "missing@example.com"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        String email    = "login_" + UUID.randomUUID() + "@example.com";
        String username = "login_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "Password123!";

        // Register first
        var reg = Map.of(
                "name", "Login User", "email", email,
                "username", username, "password", password
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // Now login
        var login = Map.of("email", email, "password", password);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        String email    = "wrongpw_" + UUID.randomUUID() + "@example.com";
        String username = "wrongpw_" + UUID.randomUUID().toString().substring(0, 8);

        var reg = Map.of(
                "name", "WrongPw User", "email", email,
                "username", username, "password", "CorrectPass1!"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        var login = Map.of("email", email, "password", "WrongPass999!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withNonExistentEmail_returns401() throws Exception {
        var login = Map.of("email", "nobody@nowhere.com", "password", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}