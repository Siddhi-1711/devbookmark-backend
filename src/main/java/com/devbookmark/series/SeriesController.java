package com.devbookmark.series;

import com.devbookmark.security.AuthUser;
import com.devbookmark.series.dto.SeriesRequest;
import com.devbookmark.series.dto.SeriesResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    // Create series
    @PostMapping
    public SeriesResponse create(
            Authentication auth,
            @Valid @RequestBody SeriesRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return seriesService.create(me, req);
    }

    // Update series
    @PutMapping("/{seriesId}")
    public SeriesResponse update(
            Authentication auth,
            @PathVariable UUID seriesId,
            @Valid @RequestBody SeriesRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return seriesService.update(me, seriesId, req);
    }

    // Get my series
    @GetMapping("/me")
    public Page<SeriesResponse> mySeries(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return seriesService.getMySeries(me, page, size);
    }

    // Get by slug (public)
    @GetMapping("/{slug}")
    public SeriesResponse getBySlug(@PathVariable String slug) {
        return seriesService.getBySlug(slug);
    }

    // Add resource to series
    @PostMapping("/{seriesId}/resources/{resourceId}")
    public SeriesResponse addResource(
            Authentication auth,
            @PathVariable UUID seriesId,
            @PathVariable UUID resourceId,
            @RequestParam int partNumber
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return seriesService.addResource(me, seriesId, resourceId, partNumber);
    }

    // Remove resource from series
    @DeleteMapping("/{seriesId}/resources/{resourceId}")
    public void removeResource(
            Authentication auth,
            @PathVariable UUID seriesId,
            @PathVariable UUID resourceId
    ) {
        UUID me = AuthUser.requireUserId(auth);
        seriesService.removeResource(me, seriesId, resourceId);
    }

    // Delete series
    @DeleteMapping("/{seriesId}")
    public void delete(
            Authentication auth,
            @PathVariable UUID seriesId
    ) {
        UUID me = AuthUser.requireUserId(auth);
        seriesService.delete(me, seriesId);
    }

    @GetMapping("/{slug}/context/{resourceId}")
    public com.devbookmark.series.dto.SeriesContextResponse context(
            @PathVariable String slug,
            @PathVariable UUID resourceId
    ) {
        return seriesService.getContext(slug, resourceId);
    }
}