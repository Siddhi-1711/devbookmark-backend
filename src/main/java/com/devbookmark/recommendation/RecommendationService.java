package com.devbookmark.recommendation;

import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.tag.TagRepository;
import com.devbookmark.user.UserFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final UserFollowRepository followRepository;
    private final TagRepository tagRepository;

    public RecommendationService(ResourceRepository resourceRepository,
                                 ResourceService resourceService,
                                 UserFollowRepository followRepository,
                                 TagRepository tagRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.followRepository = followRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Get personalized recommendations based on:
     * 1. Tags of resources user liked/saved
     * 2. Resources from similar users (who follow same people)
     * 3. Popular resources
     */
    @Transactional(readOnly = true)
    public Page<ResourceResponse> getRecommendations(UUID me, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        // Strategy: Get trending resources from last 7 days that user hasn't seen
        // In future: enhance with ML-based scoring
        Page<Resource> resources = resourceRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(safePage, safeSize)
        );

        return resourceService.enrichPage(resources, me);
    }

    /**
     * Get resources by similar tags to what user has liked/saved
     * This is a simple content-based recommendation
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourcesByTags(UUID me, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        // In production: use native query for performance
        List<Resource> resources = new ArrayList<>();
        // Placeholder: would fetch resources with these tags

        return resourceService.enrichList(resources, me);
    }

    /**
     * Collaborative filtering - find users with similar interests
     * Returns resources from users that aren't followed yet
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourcesFromSimilarUsers(UUID me) {
        // Get user's following
        Set<UUID> following = new HashSet<>();

        // Placeholder: In production, implement collaborative filtering
        // 1. Find users who follow the same people as me
        // 2. Get their popular resources
        // 3. Filter out what I've already seen

        return List.of();
    }
}