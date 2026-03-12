package com.devbookmark.resource;

import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ResourceViewService {

    private final ResourceViewRepository viewRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ResourceViewService(ResourceViewRepository viewRepository,
                               ResourceRepository resourceRepository,
                               UserRepository userRepository) {
        this.viewRepository = viewRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Track a view — called when user opens a resource/post
     * Unique per user per day (or per IP for anonymous)
     */
    @Transactional
    public void trackView(UUID resourceId, UUID userId, String ipAddress) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElse(null);
        if (resource == null) return;

        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);

        // check for duplicate view
        if (userId != null) {
            boolean alreadyViewed = viewRepository.existsRecentView(resourceId, userId, since);
            if (alreadyViewed) return;

            User user = userRepository.getReferenceById(userId);
            viewRepository.save(ResourceView.builder()
                    .resource(resource)
                    .user(user)
                    .readCompleted(false)
                    .build());
        } else {
            // anonymous user — track by IP
            if (ipAddress != null) {
                boolean alreadyViewed = viewRepository.existsRecentViewByIp(
                        resourceId, ipAddress, since
                );
                if (alreadyViewed) return;
            }

            viewRepository.save(ResourceView.builder()
                    .resource(resource)
                    .ipAddress(ipAddress)
                    .readCompleted(false)
                    .build());
        }
    }

    /**
     * Mark read as completed — called when user finishes reading
     */
    @Transactional
    public void markReadCompleted(UUID resourceId, UUID userId) {
        if (userId == null) return;

        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);

        // find today's view and mark completed
        viewRepository.findRecentViewByUser(resourceId, userId, since)
                .ifPresent(v -> {
                    v.setReadCompleted(true);
                    viewRepository.save(v);
                });
    }

    /**
     * Get stats for a resource
     */
    @Transactional(readOnly = true)
    public ResourceViewStats getStats(UUID resourceId) {
        long totalViews = viewRepository.countByResourceId(resourceId);
        long uniqueViews = viewRepository.countByResourceIdAndUserIsNotNull(resourceId);
        long reads = viewRepository.countByResourceIdAndReadCompletedTrue(resourceId);

        return new ResourceViewStats(totalViews, uniqueViews, reads);
    }

    /**
     * Get total views for a creator
     */
    @Transactional(readOnly = true)
    public long getCreatorTotalViews(UUID ownerId) {
        return viewRepository.countTotalViewsByOwner(ownerId);
    }

    public record ResourceViewStats(
            long totalViews,
            long uniqueViews,
            long reads
    ) {}
}