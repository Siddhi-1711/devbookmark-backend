package com.devbookmark.tag;

import com.devbookmark.resource.ResourceTagRepository;
import com.devbookmark.tag.dto.TrendingTagResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TagService {

    private final ResourceTagRepository resourceTagRepository;

    public TagService(ResourceTagRepository resourceTagRepository) {
        this.resourceTagRepository = resourceTagRepository;
    }

    @Transactional(readOnly = true)
    public List<TrendingTagResponse> getTrendingTags(int days, int limit) {
        int safeDays = Math.min(Math.max(days, 1), 30);
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        Instant since = Instant.now().minus(safeDays, ChronoUnit.DAYS);

        return resourceTagRepository
                .findTrendingTags(since, PageRequest.of(0, safeLimit))
                .stream()
                .map(row -> new TrendingTagResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrendingTagResponse> getPopularTags(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        return resourceTagRepository
                .findPopularTags(PageRequest.of(0, safeLimit))
                .stream()
                .map(row -> new TrendingTagResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }
}