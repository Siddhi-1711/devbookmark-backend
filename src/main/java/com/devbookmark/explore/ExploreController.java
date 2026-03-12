package com.devbookmark.explore;
import java.util.UUID;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.tag.TagFollowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import com.devbookmark.security.AuthUser;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    private final ResourceService resourceService;

    private final TrendingService trendingService;

    private final TagFollowService tagFollowService;

    public ExploreController(ResourceService resourceService,
                             TrendingService trendingService,
                             TagFollowService tagFollowService) {
        this.resourceService = resourceService;
        this.trendingService = trendingService;
        this.tagFollowService = tagFollowService;
    }

    @GetMapping("/latest")
    public Page<ResourceResponse> latest(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        var me = AuthUser.maybeUserId(auth);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // ✅ call the new latest(me, pageable)
        return resourceService.latest(me, pageable);
    }

    @GetMapping("/trending")
    public Page<ResourceResponse> trending(
            @RequestParam(defaultValue = "2") int days,  // changed from 7 to 2
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return trendingService.trending(days, page, size);
    }

    @GetMapping("/popular")
    public Page<com.devbookmark.resource.dto.ResourceResponse> popular(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return trendingService.popular(page, size);
    }

    @GetMapping("/tags")
    public Page<ResourceResponse> byFollowedTags(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        return tagFollowService.getResourcesByFollowedTags(me, PageRequest.of(safePage, safeSize));
    }
}