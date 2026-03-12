package com.devbookmark.tag;

import com.devbookmark.resource.Resource;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.tag.dto.TrendingTagResponse;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Service
public class TagFollowService {

    private final TagFollowRepository tagFollowRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    public TagFollowService(TagFollowRepository tagFollowRepository,
                            TagRepository tagRepository,
                            UserRepository userRepository,
                            ResourceRepository resourceRepository,
                            @org.springframework.context.annotation.Lazy ResourceService resourceService) {
        this.tagFollowRepository = tagFollowRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
    }

    // Follow a tag
    @Transactional
    public void followTag(UUID userId, String tagName) {
        String normalized = tagName.trim().toLowerCase();

        Tag tag = tagRepository.findByName(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + normalized));

        if (tagFollowRepository.existsByUserIdAndTagId(userId, tag.getId())) {
            return; // idempotent
        }

        User user = userRepository.getReferenceById(userId);

        tagFollowRepository.save(TagFollow.builder()
                .user(user)
                .tag(tag)
                .build());
    }

    // Unfollow a tag
    @Transactional
    public void unfollowTag(UUID userId, String tagName) {
        String normalized = tagName.trim().toLowerCase();

        tagRepository.findByName(normalized).ifPresent(tag ->
                tagFollowRepository.deleteByUserIdAndTagId(userId, tag.getId())
        );
    }

    // Get tags user follows
    @Transactional(readOnly = true)
    public List<String> getFollowedTags(UUID userId) {
        return tagFollowRepository.findFollowedTagNamesByUserId(userId);
    }

    // Get followed tags with follower counts
    @Transactional(readOnly = true)
    public List<TrendingTagResponse> getFollowedTagsWithCounts(UUID userId) {
        List<String> tagNames = tagFollowRepository.findFollowedTagNamesByUserId(userId);

        return tagNames.stream()
                .map(name -> {
                    Tag tag = tagRepository.findByName(name).orElse(null);
                    if (tag == null) return null;
                    long count = tagFollowRepository.countByTagId(tag.getId());
                    return new TrendingTagResponse(name, count);
                })
                .filter(t -> t != null)
                .toList();
    }
    @Transactional(readOnly = true)
    public Page<ResourceResponse> getResourcesByFollowedTags(UUID userId, Pageable pageable) {
        List<String> followedTags = tagFollowRepository.findFollowedTagNamesByUserId(userId);
        if (followedTags.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Resource> page = resourceRepository.findByTagNamesOrderByCreatedAtDesc(followedTags, pageable);
        return resourceService.enrichPage(page, userId);
    }
}