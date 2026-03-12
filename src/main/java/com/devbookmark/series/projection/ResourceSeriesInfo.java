package com.devbookmark.series.projection;

import java.util.UUID;

public interface ResourceSeriesInfo {

    UUID getResourceId();

    String getSlug();

    String getTitle();

    Integer getPartNumber();
}