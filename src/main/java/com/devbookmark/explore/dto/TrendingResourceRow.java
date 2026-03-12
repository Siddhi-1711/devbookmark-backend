package com.devbookmark.explore.dto;

import java.util.UUID;

public record TrendingResourceRow(
        UUID resourceId,
        long likesCount,
        long savesCount,
        long score
) { }