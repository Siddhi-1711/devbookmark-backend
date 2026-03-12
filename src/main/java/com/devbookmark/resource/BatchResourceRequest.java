package com.devbookmark.resource.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public class BatchResourceRequest {

    @NotEmpty
    private Set<UUID> resourceIds;

    public BatchResourceRequest() {}

    public BatchResourceRequest(Set<UUID> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Set<UUID> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Set<UUID> resourceIds) {
        this.resourceIds = resourceIds;
    }
}