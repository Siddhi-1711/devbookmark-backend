package com.devbookmark.resource.dto;

import com.devbookmark.resource.ResourceType;
import com.devbookmark.resource.ResourceVisibility;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String title,
        String description,
        String link,
        ResourceType type,
        Set<String> tags,
        Instant createdAt,
        Instant savedAt,

        long likeCount,
        long saveCount,
        boolean likedByMe,
        boolean savedByMe,

        String content,
        String coverImage,
        String fileUrl,
        String fileName,
        String fileContentType,
        Integer readTimeMinutes,
        boolean isPublished,
        Instant publishedAt,
        ResourceVisibility visibility,

        String seriesSlug,
        String seriesTitle,
        Integer seriesPartNumber,

        long repostCount,
        boolean repostedByMe,
        long viewCount
) { }