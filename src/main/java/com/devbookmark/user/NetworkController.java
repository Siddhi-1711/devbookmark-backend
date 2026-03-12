package com.devbookmark.user;

import com.devbookmark.security.AuthUser;
import com.devbookmark.user.dto.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
public class NetworkController {

    private final NetworkService networkService;

    public NetworkController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping("/following")
    public Page<UserSummaryResponse> following(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return networkService.myFollowing(me, page, size);
    }

    @GetMapping("/followers")
    public Page<UserSummaryResponse> followers(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return networkService.myFollowers(me, page, size);
    }
}