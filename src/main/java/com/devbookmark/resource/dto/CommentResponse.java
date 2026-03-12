package com.devbookmark.resource.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID userId,
        String userName,
        String text,
        Instant createdAt,
        Instant updatedAt,
        boolean mine,

        // NEW
        UUID parentId,
        List<CommentResponse> replies,
        int replyCount
) {}