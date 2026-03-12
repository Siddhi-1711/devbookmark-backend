package com.devbookmark.feed;

import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public Page<ResourceResponse> myFeed(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return feedService.myFeed(AuthUser.requireUserId(auth), page, size);
    }
}