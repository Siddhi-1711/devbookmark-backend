package com.devbookmark.notification.dto;

import com.devbookmark.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        UUID actorId,
        String actorName,
        UUID resourceId,      // nullable
        String resourceTitle, // nullable
        boolean read,
        Instant createdAt
) {}