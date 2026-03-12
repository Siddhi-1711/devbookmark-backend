package com.devbookmark.feed;

import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.ResourceVisibility;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.tag.TagFollowRepository;
import com.devbookmark.user.FollowService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final FollowService followService;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final TagFollowRepository tagFollowRepository;

    public FeedService(FollowService followService,
                       ResourceRepository resourceRepository,
                       ResourceService resourceService,
                       TagFollowRepository tagFollowRepository) {
        this.followService = followService;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.tagFollowRepository = tagFollowRepository;
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> myFeed(UUID me, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        var pageable = PageRequest.of(safePage, safeSize);

        // get following ids
        Set<UUID> following = followService.myFollowingIds(me, Pageable.unpaged());

        // get followed tag names
        List<String> followedTags = tagFollowRepository.findFollowedTagNamesByUserId(me);

        // if following nobody and no tags → show own content
        if (following.isEmpty() && followedTags.isEmpty()) {
            Page<Resource> mine = resourceRepository.findByOwnerIdOrderByCreatedAtDesc(me, pageable);
            return resourceService.enrichPage(mine, me);
        }

        // feed from followed users + followed tags
        Page<Resource> feed = resourceRepository.findFeedWithTags(
                me,
                following.isEmpty() ? Set.of(UUID.randomUUID()) : following,
                followedTags.isEmpty() ? List.of("__none__") : followedTags,
                Set.of(ResourceVisibility.PUBLIC, ResourceVisibility.FOLLOWERS),
                pageable
        );

        return resourceService.enrichPage(feed, me);
    }
}