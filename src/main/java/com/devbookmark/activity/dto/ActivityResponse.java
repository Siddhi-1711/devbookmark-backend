package com.devbookmark.activity.dto;

import com.devbookmark.activity.ActivityType;
import com.devbookmark.resource.dto.ResourceResponse;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID userId,
        String userName,
        ActivityType type,
        ResourceResponse resource,
        Instant createdAt
) {}