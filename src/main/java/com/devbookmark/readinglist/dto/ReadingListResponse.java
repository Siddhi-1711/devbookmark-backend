package com.devbookmark.readinglist.dto;

import com.devbookmark.resource.dto.ResourceResponse;

import java.time.Instant;
import java.util.UUID;

public record ReadingListResponse(
        UUID id,
        ResourceResponse resource,
        boolean isRead,
        Instant readAt,
        Instant addedAt
) {}