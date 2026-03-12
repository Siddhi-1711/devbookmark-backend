package com.devbookmark.user.dto;

import com.devbookmark.collection.dto.CollectionSummaryResponse;
import com.devbookmark.resource.dto.ResourceResponse;

import java.util.List;
import java.util.UUID;

public record UserPublicProfileResponse(
        UUID id,
        String name,
        String username,
        String bio,
        String avatarUrl,
        List<CollectionSummaryResponse> publicCollections,
        List<ResourceResponse> latestResources
) { }