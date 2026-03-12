package com.devbookmark.resource;

import com.devbookmark.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceViewController {

    private final ResourceViewService viewService;

    public ResourceViewController(ResourceViewService viewService) {
        this.viewService = viewService;
    }

    /**
     * Track view — call this when user opens a resource
     */
    @PostMapping("/{id}/view")
    public void trackView(
            @PathVariable UUID id,
            Authentication auth,
            HttpServletRequest request
    ) {
        UUID me = AuthUser.maybeUserId(auth);
        String ip = getClientIp(request);
        viewService.trackView(id, me, ip);
    }

    /**
     * Mark read completed — call when user finishes reading
     */
    @PostMapping("/{id}/read-completed")
    public void markReadCompleted(
            @PathVariable UUID id,
            Authentication auth
    ) {
        UUID me = AuthUser.maybeUserId(auth);
        viewService.markReadCompleted(id, me);
    }

    /**
     * Get view stats for a resource (owner only)
     */
    @GetMapping("/{id}/stats")
    public ResourceViewService.ResourceViewStats getStats(
            @PathVariable UUID id,
            Authentication auth
    ) {
        return viewService.getStats(id);
    }

    /**
     * Get total views for creator dashboard
     */
    @GetMapping("/creator/stats")
    public long creatorStats(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return viewService.getCreatorTotalViews(me);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}