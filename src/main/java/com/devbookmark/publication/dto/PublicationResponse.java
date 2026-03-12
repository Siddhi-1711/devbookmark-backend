package com.devbookmark.publication.dto;

import java.time.Instant;
import java.util.UUID;

public record PublicationResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String slug,
        String name,
        String bio,
        String logoUrl,
        boolean isActive,
        Instant createdAt,
        long followerCount,
        long postCount
) {}