package com.devbookmark.series;

import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.series.dto.SeriesContextResponse;
import com.devbookmark.series.dto.SeriesRequest;
import com.devbookmark.series.dto.SeriesResponse;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesItemRepository seriesItemRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public SeriesService(SeriesRepository seriesRepository,
                         SeriesItemRepository seriesItemRepository,
                         ResourceRepository resourceRepository,
                         UserRepository userRepository) {
        this.seriesRepository = seriesRepository;
        this.seriesItemRepository = seriesItemRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // Create series
    @Transactional
    public SeriesResponse create(UUID ownerId, SeriesRequest req) {
        if (seriesRepository.existsBySlug(req.slug())) {
            throw new IllegalArgumentException("Slug already taken.");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Series s = Series.builder()
                .owner(owner)
                .title(req.title().trim())
                .description(req.description())
                .slug(req.slug().trim().toLowerCase())
                .coverImage(req.coverImage())
                .isComplete(req.isComplete())
                .build();

        return toResponse(seriesRepository.save(s));
    }

    // Update series
    @Transactional
    public SeriesResponse update(UUID ownerId, UUID seriesId, SeriesRequest req) {
        Series s = seriesRepository.findByIdAndOwnerId(seriesId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Series not found."));

        String newSlug = req.slug().trim().toLowerCase();
        if (!s.getSlug().equals(newSlug) && seriesRepository.existsBySlug(newSlug)) {
            throw new IllegalArgumentException("Slug already taken.");
        }

        s.setTitle(req.title().trim());
        s.setDescription(req.description());
        s.setSlug(newSlug);
        s.setCoverImage(req.coverImage());
        s.setComplete(req.isComplete());

        return toResponse(s);
    }

    // Add resource to series
    @Transactional
    public SeriesResponse addResource(UUID ownerId, UUID seriesId, UUID resourceId, int partNumber) {
        Series s = seriesRepository.findByIdAndOwnerId(seriesId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Series not found."));

        if (seriesItemRepository.existsByResourceId(resourceId)) {
            throw new IllegalArgumentException("This resource is already part of another series.");
        }

        if (seriesItemRepository.existsBySeriesIdAndResourceId(seriesId, resourceId)) {
            throw new IllegalArgumentException("Resource already in series.");
        }

        if (seriesItemRepository.existsBySeriesIdAndPartNumber(seriesId, partNumber)) {
            throw new IllegalArgumentException("Part number already taken.");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        seriesItemRepository.save(SeriesItem.builder()
                .series(s)
                .resource(resource)
                .partNumber(partNumber)
                .build());

        return toResponse(seriesRepository.findDetailById(seriesId).orElse(s));
    }

    // Remove resource from series
    @Transactional
    public void removeResource(UUID ownerId, UUID seriesId, UUID resourceId) {
        seriesRepository.findByIdAndOwnerId(seriesId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Series not found."));

        seriesItemRepository.deleteBySeriesIdAndResourceId(seriesId, resourceId);
    }

    // Get by slug (public)
    @Transactional(readOnly = true)
    public SeriesResponse getBySlug(String slug) {
        Series s = seriesRepository.findDetailBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Series not found."));
        return toResponse(s);
    }

    // Get my series
    @Transactional(readOnly = true)
    public Page<SeriesResponse> getMySeries(UUID ownerId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        return seriesRepository.findByOwnerIdOrderByCreatedAtDesc(
                ownerId, PageRequest.of(safePage, safeSize)
        ).map(this::toResponse);
    }

    // Delete series
    @Transactional
    public void delete(UUID ownerId, UUID seriesId) {
        Series s = seriesRepository.findByIdAndOwnerId(seriesId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Series not found."));
        seriesRepository.delete(s);
    }

    private SeriesResponse toResponse(Series s) {
        List<SeriesResponse.SeriesPart> parts = s.getItems().stream()
                .map(item -> new SeriesResponse.SeriesPart(
                        item.getId(),
                        item.getPartNumber(),
                        item.getResource().getId(),
                        item.getResource().getTitle(),
                        item.getResource().getDescription(),
                        item.getResource().getType(),
                        item.getResource().isPublished(),
                        item.getResource().getPublishedAt()
                ))
                .toList();

        return new SeriesResponse(
                s.getId(),
                s.getOwner().getId(),
                s.getOwner().getName(),
                s.getTitle(),
                s.getDescription(),
                s.getSlug(),
                s.getCoverImage(),
                s.isComplete(),
                parts.size(),
                parts,
                s.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public SeriesContextResponse getContext(String slug, UUID resourceId) {
        Series s = seriesRepository.findDetailBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Series not found."));

        // find current part
        var parts = s.getItems();

        SeriesItem current = parts.stream()
                .filter(it -> it.getResource().getId().equals(resourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resource not in this series."));

        int currentPart = current.getPartNumber();

        SeriesItem prev = parts.stream()
                .filter(it -> it.getPartNumber() == currentPart - 1)
                .findFirst()
                .orElse(null);

        SeriesItem next = parts.stream()
                .filter(it -> it.getPartNumber() == currentPart + 1)
                .findFirst()
                .orElse(null);

        return new SeriesContextResponse(
                s.getSlug(),
                s.getTitle(),
                currentPart,
                prev != null ? prev.getPartNumber() : null,
                prev != null ? prev.getResource().getId() : null,
                next != null ? next.getPartNumber() : null,
                next != null ? next.getResource().getId() : null
        );
    }
}