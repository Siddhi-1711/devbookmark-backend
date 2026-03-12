package com.devbookmark.user.dto;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name,
        String username,
        String avatarUrl,
        long followerCount,
        boolean followedByMe
) {}