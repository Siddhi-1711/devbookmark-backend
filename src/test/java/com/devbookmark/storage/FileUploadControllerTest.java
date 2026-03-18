package com.devbookmark.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
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
class FileUploadControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        String email    = "upload_" + UUID.randomUUID() + "@example.com";
        String username = "upload_" + UUID.randomUUID().toString().substring(0, 8);

        var body = Map.of(
                "name", "Upload User", "email", email,
                "username", username, "password", "Password123!"
        );

        var result = mockMvc.perform(multipart("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();

        // fallback: use post() if multipart doesn't work for JSON
        if (result.getResponse().getStatus() != 200) {
            result = mockMvc.perform(
                            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                    .post("/api/auth/register")
                                    .contentType("application/json")
                                    .content(objectMapper.writeValueAsString(body)))
                    .andReturn();
        }

        var json = objectMapper.readTree(result.getResponse().getContentAsString());
        userToken = json.get("token").asText();
    }

    // ─── Auth guard ───────────────────────────────────────────────────────────

    @Test
    void upload_withNoToken_returns401() throws Exception {
        var file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/upload/file").file(file))
                .andExpect(status().isUnauthorized());
    }

    // ─── File type validation ─────────────────────────────────────────────────

    @Test
    void upload_withDisallowedFileType_returns400() throws Exception {
        // .exe disguised — should be blocked by MIME allowlist or magic bytes
        var file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream",
                new byte[]{0x4D, 0x5A, 0x00, 0x00} // MZ header = Windows executable
        );

        mockMvc.perform(multipart("/api/upload/file")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_withExeRenamedAsPdf_returns400() throws Exception {
        // Magic bytes check: file claims to be PDF but has MZ (exe) header
        var file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf",
                new byte[]{0x4D, 0x5A, 0x00, 0x00} // MZ header, not %PDF
        );

        mockMvc.perform(multipart("/api/upload/file")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    // ─── File size validation ─────────────────────────────────────────────────

    @Test
    void upload_withOversizedFile_returns400OrPayloadTooLarge() throws Exception {
        // 11 MB — over the 10 MB limit
        byte[] bigFile = new byte[11 * 1024 * 1024];
        var file = new MockMultipartFile(
                "file", "big.png", "image/png", bigFile
        );

        mockMvc.perform(multipart("/api/upload/file")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(result ->
                        org.junit.jupiter.api.Assertions.assertTrue(
                                result.getResponse().getStatus() == 400 ||
                                        result.getResponse().getStatus() == 413,
                                "Expected 400 or 413 for oversized file, got: "
                                        + result.getResponse().getStatus()
                        )
                );
    }
}