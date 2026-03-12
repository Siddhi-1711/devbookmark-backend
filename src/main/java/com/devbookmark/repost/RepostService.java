package com.devbookmark.repost;

import com.devbookmark.notification.NotificationService;
import com.devbookmark.repost.dto.RepostResponse;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.user.FollowService;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RepostService {

    private final RepostRepository repostRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FollowService followService;

    public RepostService(RepostRepository repostRepository,
                         ResourceRepository resourceRepository,
                         ResourceService resourceService,
                         UserRepository userRepository,
                         NotificationService notificationService,
                         FollowService followService) {
        this.repostRepository = repostRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.followService = followService;
    }

    // Repost a resource
    @Transactional
    public RepostResponse repost(UUID userId, UUID resourceId, String comment) {
        // cant repost own resource
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

// ADD THIS - cannot repost private resources
        if (resource.getVisibility() == com.devbookmark.resource.ResourceVisibility.PRIVATE) {
            throw new IllegalArgumentException("Cannot repost a private resource.");
        }

        if (resource.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot repost your own resource.");
        }

        // idempotent
        if (repostRepository.existsByUserIdAndResourceId(userId, resourceId)) {
            throw new IllegalArgumentException("You already reposted this.");
        }

        User user = userRepository.getReferenceById(userId);

        Repost saved = repostRepository.save(Repost.builder()
                .user(user)
                .resource(resource)
                .comment(comment)
                .build());

        // notify original owner
        notificationService.notifyRepost(userId, resourceId);

        return toResponse(saved);
    }

    // Undo repost
    @Transactional
    public void undoRepost(UUID userId, UUID resourceId) {
        repostRepository.findByUserIdAndResourceId(userId, resourceId)
                .ifPresent(repostRepository::delete);
    }

    // Get my reposts
    @Transactional(readOnly = true)
    public Page<RepostResponse> myReposts(UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        return repostRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(safePage, safeSize))
                .map(this::toResponse);
    }

    // Get reposts from following (for feed)
    @Transactional(readOnly = true)
    public Page<RepostResponse> followingReposts(UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        var following = followService.myFollowingIds(userId, Pageable.unpaged());
        if (following.isEmpty()) {
            return Page.empty(PageRequest.of(safePage, safeSize));
        }

        return repostRepository
                .findByFollowingIds(following, PageRequest.of(safePage, safeSize))
                .map(this::toResponse);
    }

    // Get repost count for resource
    @Transactional(readOnly = true)
    public long repostCount(UUID resourceId) {
        return repostRepository.countByResourceId(resourceId);
    }

    private RepostResponse toResponse(Repost r) {
        return new RepostResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getName(),
                r.getComment(),
                resourceService.toResponse(r.getResource()),
                r.getCreatedAt()
        );
    }
}