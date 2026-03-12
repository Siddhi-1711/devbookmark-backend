package com.devbookmark.tag;

import com.devbookmark.security.AuthUser;
import com.devbookmark.tag.dto.TrendingTagResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
public class TagFollowController {

    private final TagFollowService tagFollowService;

    public TagFollowController(TagFollowService tagFollowService) {
        this.tagFollowService = tagFollowService;
    }

    // Follow a tag
    @PostMapping("/{tagName}/follow")
    public void follow(Authentication auth, @PathVariable String tagName) {
        UUID me = AuthUser.requireUserId(auth);
        tagFollowService.followTag(me, tagName);
    }

    // Unfollow a tag
    @DeleteMapping("/{tagName}/follow")
    public void unfollow(Authentication auth, @PathVariable String tagName) {
        UUID me = AuthUser.requireUserId(auth);
        tagFollowService.unfollowTag(me, tagName);
    }

    // Get my followed tags
    @GetMapping("/following")
    public List<TrendingTagResponse> myFollowedTags(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return tagFollowService.getFollowedTagsWithCounts(me);
    }
}