package com.devbookmark.series.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SeriesRequest(
        @NotBlank @Size(min = 3, max = 160) String title,
        @Size(max = 500) String description,
        @Size(max = 2048) String coverImage,

        @NotBlank
        @Size(min = 2, max = 200)
        @Pattern(regexp = "^[a-z0-9-]+$",
                message = "Slug can only contain lowercase letters, numbers and hyphens")
        String slug,

        boolean isComplete
) {}