package com.devbookmark.search;

import com.devbookmark.resource.*;
import com.devbookmark.resource.dto.ResourceResponse;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class SearchService {

    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    private final ResourceLikeRepository likeRepository;

    public SearchService(ResourceRepository resourceRepository,
                         ResourceService resourceService,
                         ResourceLikeRepository likeRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.likeRepository = likeRepository;
    }
    @Transactional(readOnly = true)
    public Page<ResourceResponse> searchResources(
            UUID me, String q, String tagsCsv, ResourceType type,
            String author, String from, String to,
            String sort, boolean publishedOnly,
            int page, int size) {

        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        Set<String> tags = parseTags(tagsCsv);
        Instant fromDate = parseDate(from, true);
        Instant toDate = parseDate(to, false);
        Set<ResourceVisibility> visibilities = getVisibilityForUser(me);

        Sort sortOrder = switch (sort) {
            case "oldest" -> Sort.by("createdAt").ascending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(safePage, safeSize, sortOrder);

        // use original search method
        Page<Resource> result = resourceRepository.search(
                (q == null || q.isBlank()) ? null : q.trim(),
                type,
                tags.isEmpty(),
                tags,
                visibilities,
                pageable
        );

        // filter in memory for author, date, publishedOnly
        // (small result sets only — optimize later with native query)
        String authorLower = (author == null || author.isBlank()) ? null : author.trim().toLowerCase();
        Instant finalFromDate = fromDate;
        Instant finalToDate = toDate;

        List<Resource> filtered = result.getContent().stream()
                .filter(r -> authorLower == null ||
                        r.getOwner().getName().toLowerCase().contains(authorLower))
                .filter(r -> finalFromDate == null || !r.getCreatedAt().isBefore(finalFromDate))
                .filter(r -> finalToDate == null || !r.getCreatedAt().isAfter(finalToDate))
                .filter(r -> !publishedOnly || r.isPublished())
                .toList();

// Make it mutable
        List<Resource> finalList = new ArrayList<>(filtered);

// Apply sort AFTER filtering (important)
        if ("popular".equals(sort)) {
            finalList.sort((a, b) -> Long.compare(
                    likeRepository.countByResourceId(b.getId()),
                    likeRepository.countByResourceId(a.getId())
            ));
        } else if ("oldest".equals(sort)) {
            finalList.sort(Comparator.comparing(Resource::getCreatedAt));
        } else { // latest default
            finalList.sort(Comparator.comparing(Resource::getCreatedAt).reversed());
        }

        List<ResourceResponse> enriched = resourceService.enrichList(finalList, me);

// IMPORTANT: total should match what you actually return after filtering
        return new PageImpl<>(enriched, pageable, finalList.size());
    }

    private Instant parseDate(String date, boolean startOfDay) {
        if (date == null || date.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(date);
            return startOfDay
                    ? d.atStartOfDay().toInstant(ZoneOffset.UTC)
                    : d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    private Set<ResourceVisibility> getVisibilityForUser(UUID me) {
        if (me == null) return Set.of(ResourceVisibility.PUBLIC);
        return Set.of(ResourceVisibility.PUBLIC, ResourceVisibility.FOLLOWERS);
    }

    private Set<String> parseTags(String tagsCsv) {
        if (tagsCsv == null || tagsCsv.isBlank()) return Set.of();
        String[] parts = tagsCsv.split(",");
        Set<String> out = new LinkedHashSet<>();
        for (String p : parts) {
            String t = p.trim().toLowerCase();
            if (!t.isBlank()) out.add(t);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> suggestions(String q) {
        if (q == null) return Map.of();
        String query = q.trim().toLowerCase();
        if (query.length() < 2) return Map.of("titles", List.of(), "tags", List.of());

        int limit = 8;
        List<String> titles = resourceRepository.findTitleSuggestions(
                query, PageRequest.of(0, limit));
        List<String> tags = resourceRepository.findTagSuggestions(
                query, PageRequest.of(0, limit));

        return Map.of("titles", titles, "tags", tags);
    }

    @Transactional(readOnly = true)
    public List<String> authorSuggestions(String q) {
        if (q == null || q.isBlank()) return List.of();
        return resourceRepository.findAuthorSuggestions(
                q.trim().toLowerCase(), PageRequest.of(0, 8));
    }
}