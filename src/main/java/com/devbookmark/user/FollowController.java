package com.devbookmark.user;

import com.devbookmark.security.AuthUser;
import com.devbookmark.user.dto.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class FollowController {

    private final FollowService followService;
    private final NetworkService networkService;

    public FollowController(FollowService followService, NetworkService networkService) {
        this.followService = followService;
        this.networkService = networkService;
    }

    @PostMapping("/{id}/follow")
    public void follow(Authentication auth, @PathVariable UUID id) {
        followService.follow(AuthUser.requireUserId(auth), id);
    }

    @DeleteMapping("/{id}/follow")
    public void unfollow(Authentication auth, @PathVariable UUID id) {
        followService.unfollow(AuthUser.requireUserId(auth), id);
    }

    @GetMapping("/{userId}/followers")
    public Page<com.devbookmark.user.dto.UserSummaryResponse> followers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return networkService.userFollowers(userId, page, size);
    }

    @GetMapping("/{userId}/following")
    public Page<UserSummaryResponse> following(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return networkService.userFollowing(userId, page, size);
    }
}