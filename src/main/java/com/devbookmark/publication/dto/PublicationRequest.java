package com.devbookmark.publication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublicationRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 500) String bio,
        @Size(max = 2048) String logoUrl,

        // slug: only lowercase letters, numbers, hyphens
        @NotBlank
        @Size(min = 2, max = 100)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers and hyphens")
        String slug
) {}