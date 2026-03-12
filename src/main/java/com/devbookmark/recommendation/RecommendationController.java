package com.devbookmark.recommendation;

import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Get personalized recommendations for logged-in user
     */
    @GetMapping
    public Page<ResourceResponse> getRecommendations(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return recommendationService.getRecommendations(me, page, size);
    }

    /**
     * Get resources by specific tags
     */
    @GetMapping("/by-tags")
    public List<ResourceResponse> getByTags(
            Authentication auth,
            @RequestParam Set<String> tags
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return recommendationService.getResourcesByTags(me, tags);
    }
}