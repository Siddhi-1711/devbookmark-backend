package com.devbookmark.repost;

import com.devbookmark.repost.dto.RepostResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reposts")
public class RepostController {

    private final RepostService repostService;

    public RepostController(RepostService repostService) {
        this.repostService = repostService;
    }

    // Repost a resource
    @PostMapping("/{resourceId}")
    public RepostResponse repost(
            Authentication auth,
            @PathVariable UUID resourceId,
            @RequestParam(required = false) String comment
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return repostService.repost(me, resourceId, comment);
    }

    // Undo repost
    @DeleteMapping("/{resourceId}")
    public void undoRepost(
            Authentication auth,
            @PathVariable UUID resourceId
    ) {
        UUID me = AuthUser.requireUserId(auth);
        repostService.undoRepost(me, resourceId);
    }

    // Get my reposts
    @GetMapping("/me")
    public Page<RepostResponse> myReposts(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return repostService.myReposts(me, page, size);
    }

    // Get reposts from people I follow
    @GetMapping("/following")
    public Page<RepostResponse> followingReposts(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return repostService.followingReposts(me, page, size);
    }

    // Get repost count for a resource
    @GetMapping("/{resourceId}/count")
    public long repostCount(@PathVariable UUID resourceId) {
        return repostService.repostCount(resourceId);
    }
}