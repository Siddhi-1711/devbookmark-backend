package com.devbookmark.collection.dto;

import com.devbookmark.resource.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CollectionDetailResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String name,
        String description,
        boolean isPublic,
        Instant createdAt,
        List<Item> items
) {
    public record Item(
            UUID resourceId,
            String title,
            String description,
            String link,
            ResourceType type,
            Set<String> tags,
            Instant addedAt
    ) {}
}