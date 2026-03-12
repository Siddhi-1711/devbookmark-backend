package com.devbookmark.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record MarkReadRequest(
        @NotNull Set<UUID> ids
) {}