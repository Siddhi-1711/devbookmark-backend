package com.devbookmark.profile;

import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String name,
        String email,       // null for other users' profiles
        String username,
        String bio,
        String avatarUrl,
        long followers,
        long following,
        long resources,
        boolean followedByMe,
        String publicationSlug
) {}