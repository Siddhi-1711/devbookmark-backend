package com.devbookmark.suggestion;

import com.devbookmark.security.AuthUser;
import com.devbookmark.user.dto.UserSummaryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suggestions")
public class UserSuggestionController {

    private final UserSuggestionService userSuggestionService;

    public UserSuggestionController(UserSuggestionService userSuggestionService) {
        this.userSuggestionService = userSuggestionService;
    }

    /**
     * Get user suggestions for who to follow
     * Based on 2nd degree connections and shared interests
     */
    @GetMapping("/users")
    public List<UserSummaryResponse> getUserSuggestions(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return userSuggestionService.getSuggestions(me, limit);
    }

    /**
     * Get trending/active users
     */
    @GetMapping("/users/trending")
    public List<UserSummaryResponse> getTrendingUsers(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return userSuggestionService.getTrendingUsers(limit);
    }
}