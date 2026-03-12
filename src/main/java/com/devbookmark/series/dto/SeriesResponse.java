package com.devbookmark.series.dto;

import com.devbookmark.resource.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeriesResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String title,
        String description,
        String slug,
        String coverImage,
        boolean isComplete,
        int totalParts,
        List<SeriesPart> parts,
        Instant createdAt
) {
    public record SeriesPart(
            UUID id,
            int partNumber,
            UUID resourceId,
            String resourceTitle,
            String resourceDescription,
            ResourceType resourceType,
            boolean isPublished,
            Instant publishedAt
    ) {}
}