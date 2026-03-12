package com.devbookmark.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentCreateRequest(
        @NotBlank @Size(min = 1, max = 500) String text,

        // optional - if replying to a comment
        UUID parentId
) {}