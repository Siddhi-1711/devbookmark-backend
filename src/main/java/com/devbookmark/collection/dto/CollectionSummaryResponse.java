package com.devbookmark.collection.dto;

import java.time.Instant;
import java.util.UUID;

public record CollectionSummaryResponse(
        UUID id,
        String name,
        String description,
        boolean isPublic,
        long itemCount,
        Instant createdAt
) { }