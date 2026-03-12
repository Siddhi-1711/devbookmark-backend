package com.devbookmark.resource.dto;

import com.devbookmark.resource.ResourceType;
import com.devbookmark.resource.ResourceVisibility;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ResourceUpdateRequest(
        @Size(min = 3, max = 160) String title,
        @Size(max = 500) String description,
        @Size(max = 2048) String link,
        @Size(max = 255) String fileName,
        @Size(max = 100) String fileContentType,
        ResourceType type,
        Set<@Size(min = 1, max = 50) String> tags,
        ResourceVisibility visibility,
        String content,
        @Size(max = 2048) String coverImage,
        @Size(max = 2048) String fileUrl,
        Boolean publish  // true = publish, false = unpublish
) {}