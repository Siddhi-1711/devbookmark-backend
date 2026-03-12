package com.devbookmark.profile;

import com.devbookmark.resource.ResourceVisibility;
import com.devbookmark.security.AuthUser;
import com.devbookmark.user.UserRepository;
import com.devbookmark.user.UserFollowRepository;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.resource.ResourceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.devbookmark.publication.PublicationRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final PublicationRepository publicationRepository;

    public ProfileController(
            UserRepository userRepository,
            UserFollowRepository userFollowRepository,
            ResourceRepository resourceRepository,
            ResourceService resourceService,
            PublicationRepository publicationRepository
    ) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.publicationRepository = publicationRepository;
    }

    @GetMapping("/me")
    public ProfileResponse me(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return buildProfile(me, me);
    }

    @GetMapping("/{userId}")
    public ProfileResponse user(@PathVariable UUID userId, Authentication auth) {
        UUID viewerId = AuthUser.maybeUserId(auth);
        return buildProfile(userId, viewerId);
    }

    @GetMapping("/{userId}/resources")
    public List<ResourceResponse> resources(
            @PathVariable UUID userId,
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID viewerId = AuthUser.maybeUserId(auth);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        boolean isOwner = userId.equals(viewerId);
        boolean isFollower = viewerId != null &&
                userFollowRepository.existsByFollowerIdAndFolloweeId(viewerId, userId);

        // Fetch resources then filter by visibility
        var resources = resourceRepository
                .findProfileResources(userId, PageRequest.of(safePage, safeSize))
                .stream()
                .filter(r -> {
                    if (isOwner) return true;
                    if (r.getVisibility() == ResourceVisibility.PUBLIC) return true;
                    if (r.getVisibility() == ResourceVisibility.FOLLOWERS && isFollower) return true;
                    return false;
                })
                .toList();

        // Use enrichList (batch) instead of toResponse() in a loop — fixes N+1
        return resourceService.enrichList(resources, viewerId);
    }

    private ProfileResponse buildProfile(UUID userId, UUID viewerId) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        long followers = userFollowRepository.countByFolloweeId(userId);
        long following  = userFollowRepository.countByFollowerId(userId);
        long resources  = resourceRepository.countByOwnerId(userId);
        boolean followedByMe = viewerId != null && !viewerId.equals(userId) &&
                userFollowRepository.existsByFollowerIdAndFolloweeId(viewerId, userId);

        String publicationSlug = publicationRepository.findByOwnerId(userId)
                .map(p -> p.getSlug())
                .orElse(null);

        // Only expose email on own profile
        String email = userId.equals(viewerId) ? u.getEmail() : null;

        return new ProfileResponse(
                u.getId(),
                u.getName(),
                email,
                u.getUsername(),
                u.getBio(),
                u.getAvatarUrl(),
                followers,
                following,
                resources,
                followedByMe,
                publicationSlug
        );
    }
}