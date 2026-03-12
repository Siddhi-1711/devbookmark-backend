package com.devbookmark.repost.dto;

import com.devbookmark.resource.dto.ResourceResponse;

import java.time.Instant;
import java.util.UUID;

public record RepostResponse(
        UUID id,
        UUID repostedBy,
        String repostedByName,
        String comment,
        ResourceResponse resource,
        Instant createdAt
) {}