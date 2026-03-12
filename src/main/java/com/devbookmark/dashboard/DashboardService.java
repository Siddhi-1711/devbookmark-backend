package com.devbookmark.dashboard;

import com.devbookmark.dashboard.dto.DashboardResponse;
import com.devbookmark.resource.*;
import com.devbookmark.user.UserFollowRepository;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final UserFollowRepository followRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceLikeRepository likeRepository;
    private final ResourceSaveRepository saveRepository;
    private final ResourceCommentRepository commentRepository;
    private final ResourceViewRepository viewRepository;

    public DashboardService(UserRepository userRepository,
                            UserFollowRepository followRepository,
                            ResourceRepository resourceRepository,
                            ResourceLikeRepository likeRepository,
                            ResourceSaveRepository saveRepository,
                            ResourceCommentRepository commentRepository,
                            ResourceViewRepository viewRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.resourceRepository = resourceRepository;
        this.likeRepository = likeRepository;
        this.saveRepository = saveRepository;
        this.commentRepository = commentRepository;
        this.viewRepository = viewRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID me) {
        var user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        // Profile stats
        long followers = followRepository.countByFolloweeId(me);
        long following = followRepository.countByFollowerId(me);

        // Content stats
        long totalPosts = resourceRepository.countByOwnerIdAndType(
                me, ResourceType.WRITTEN_POST);
        long totalResources = resourceRepository.countByOwnerId(me) - totalPosts;
        long publishedPosts = resourceRepository.countByOwnerIdAndTypeAndIsPublished(
                me, ResourceType.WRITTEN_POST, true);
        long draftPosts = totalPosts - publishedPosts;

        // Engagement stats
        long totalViews = viewRepository.countTotalViewsByOwner(me);
        long totalLikes = likeRepository.countByResourceOwnerId(me);
        long totalSaves = saveRepository.countByResourceOwnerId(me);
        long totalComments = commentRepository.countByResourceOwnerId(me);

        // This week stats
        long newFollowersThisWeek = followRepository.countByFolloweeIdAndCreatedAtAfter(
                me, weekAgo);
        long newViewsThisWeek = viewRepository.countByResourceOwnerIdAndCreatedAtAfter(
                me, weekAgo);
        long newLikesThisWeek = likeRepository.countByResourceOwnerIdAndCreatedAtAfter(
                me, weekAgo);

        // Top posts
        List<DashboardResponse.TopPost> topPosts = getTopPosts(me);

        return new DashboardResponse(
                user.getId(),
                user.getName(),
                followers,
                following,
                totalPosts,
                totalResources,
                publishedPosts,
                draftPosts,
                totalViews,
                totalLikes,
                totalSaves,
                totalComments,
                topPosts,
                newFollowersThisWeek,
                newViewsThisWeek,
                newLikesThisWeek
        );
    }

    private List<DashboardResponse.TopPost> getTopPosts(UUID me) {
        var resources = resourceRepository.findByOwnerIdOrderByCreatedAtDesc(
                me, PageRequest.of(0, 10)
        ).getContent();

        List<DashboardResponse.TopPost> posts = new ArrayList<>();
        for (var r : resources) {
            long views = viewRepository.countByResourceId(r.getId());
            long likes = likeRepository.countByResourceId(r.getId());
            long saves = saveRepository.countByResourceId(r.getId());
            long comments = commentRepository.countByResourceId(r.getId());

            posts.add(new DashboardResponse.TopPost(
                    r.getId(),
                    r.getTitle(),
                    views,
                    likes,
                    saves,
                    comments,
                    r.getPublishedAt()
            ));
        }

        // sort by views desc
        posts.sort((a, b) -> Long.compare(b.views(), a.views()));
        return posts;
    }
}