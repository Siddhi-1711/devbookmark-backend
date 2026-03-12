package com.devbookmark.activity;

import com.devbookmark.activity.dto.ActivityResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Get activity feed - what people you follow are doing
     */
    @GetMapping("/feed")
    public Page<ActivityResponse> getActivityFeed(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return activityService.getActivityFeed(me, page, size);
    }

    /**
     * Get user's public timeline
     */
    @GetMapping("/users/{userId}")
    public Page<ActivityResponse> getUserTimeline(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return activityService.getUserTimeline(userId, page, size);
    }
}