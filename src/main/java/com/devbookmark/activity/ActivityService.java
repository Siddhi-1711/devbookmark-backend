package com.devbookmark.activity;

import com.devbookmark.activity.dto.ActivityResponse;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.user.FollowService;
import com.devbookmark.user.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final FollowService followService;
    private final ResourceService resourceService;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           FollowService followService,
                           @Lazy ResourceService resourceService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.followService = followService;
        this.resourceService = resourceService;
    }

    @Transactional
    public void logActivityResourceCreated(UUID userId, Resource resource) {
        activityRepository.save(Activity.builder()
                .user(userRepository.getReferenceById(userId))
                .resource(resource)
                .type(ActivityType.CREATED_RESOURCE)
                .build());
    }

    @Transactional
    public void logActivityLike(UUID userId, Resource resource) {
        activityRepository.save(Activity.builder()
                .user(userRepository.getReferenceById(userId))
                .resource(resource)
                .type(ActivityType.LIKED_RESOURCE)
                .build());
    }

    @Transactional
    public void logActivitySave(UUID userId, Resource resource) {
        activityRepository.save(Activity.builder()
                .user(userRepository.getReferenceById(userId))
                .resource(resource)
                .type(ActivityType.SAVED_RESOURCE)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getActivityFeed(UUID me, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        Set<UUID> followingIds = followService.myFollowingIds(me, PageRequest.of(0, 500));

        if (followingIds.isEmpty()) {
            return Page.empty(PageRequest.of(safePage, safeSize));
        }

        Page<Activity> activities = activityRepository.findFollowingActivities(
                followingIds,
                Set.of(ActivityType.CREATED_RESOURCE, ActivityType.LIKED_RESOURCE, ActivityType.SAVED_RESOURCE),
                PageRequest.of(safePage, safeSize)
        );

        return activities.map(a -> toResponse(a, me));
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getUserTimeline(UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        Page<Activity> activities = activityRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(safePage, safeSize)
        );

        return activities.map(a -> toResponse(a, null));
    }

    private ActivityResponse toResponse(Activity a, UUID me) {
        return new ActivityResponse(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getName(),
                a.getType(),
                a.getResource() != null ? resourceService.toResponse(a.getResource()) : null,
                a.getCreatedAt()
        );
    }
}