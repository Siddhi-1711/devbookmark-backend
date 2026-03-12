package com.devbookmark.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionUpdateRequest(
        @NotBlank @Size(min = 2, max = 80) String name,
        @Size(max = 300) String description,
        Boolean isPublic
) { }