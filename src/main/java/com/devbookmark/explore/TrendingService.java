package com.devbookmark.explore;

import com.devbookmark.explore.dto.TrendingResourceRow;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.dto.ResourceResponse;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TrendingService {

    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    public TrendingService(ResourceRepository resourceRepository, ResourceService resourceService) {
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> trending(int days, int page, int size) {
        int safeDays = Math.min(Math.max(days, 1), 30);   // 1..30
        int safeSize = Math.min(Math.max(size, 1), 50);   // 1..50

        Instant since = Instant.now().minus(safeDays, ChronoUnit.DAYS);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<TrendingResourceRow> rows = resourceRepository.findTrending(since, pageable);

        // Load resources in same order
        List<UUID> ids = rows.getContent().stream().map(TrendingResourceRow::resourceId).toList();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Resource> resources = resourceRepository.findAllById(ids);
        Map<UUID, Resource> map = new HashMap<>();
        for (Resource r : resources) map.put(r.getId(), r);

        // Keep trending order, then batch-enrich (single DB round-trip for all counts)
        List<Resource> ordered = ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();

        List<ResourceResponse> out = resourceService.enrichList(ordered, null);

        return new PageImpl<>(out, pageable, rows.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<com.devbookmark.resource.dto.ResourceResponse> popular(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        var rows = resourceRepository.findPopular(PageRequest.of(safePage, safeSize));

        // Fetch resources by ids and map to response (keep ranking order)
        List<UUID> ids = rows.getContent().stream().map(TrendingResourceRow::resourceId).toList();
        if (ids.isEmpty()) {
            return Page.empty(PageRequest.of(safePage, safeSize));
        }

        List<Resource> resources = resourceRepository.findAllById(ids);
        Map<UUID, Resource> map = new HashMap<>();
        for (Resource r : resources) map.put(r.getId(), r);

        // Keep popularity order, then batch-enrich (single DB round-trip for all counts)
        List<Resource> ordered = ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();

        List<com.devbookmark.resource.dto.ResourceResponse> ordered2 =
                resourceService.enrichList(ordered, null);

        return new PageImpl<>(ordered2, PageRequest.of(safePage, safeSize), rows.getTotalElements());
    }
}