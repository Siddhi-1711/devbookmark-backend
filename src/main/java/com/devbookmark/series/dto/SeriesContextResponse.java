package com.devbookmark.series.dto;

import java.util.UUID;

public record SeriesContextResponse(
        String seriesSlug,
        String seriesTitle,
        int partNumber,
        Integer prevPartNumber,
        UUID prevResourceId,
        Integer nextPartNumber,
        UUID nextResourceId
) {}