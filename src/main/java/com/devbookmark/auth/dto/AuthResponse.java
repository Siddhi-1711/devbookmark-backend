package com.devbookmark.auth.dto;

import com.devbookmark.user.UserRole;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        UserResponse user
) {
    public record UserResponse(
            UUID id,
            String name,
            String email,
            UserRole role
    ) {}
}