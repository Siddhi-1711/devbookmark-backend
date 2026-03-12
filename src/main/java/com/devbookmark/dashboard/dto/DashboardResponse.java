package com.devbookmark.dashboard.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(

        // Profile summary
        UUID userId,
        String name,
        long followers,
        long following,

        // Content stats
        long totalPosts,
        long totalResources,
        long publishedPosts,
        long draftPosts,

        // Engagement stats
        long totalViews,
        long totalLikes,
        long totalSaves,
        long totalComments,

        // Top performing posts
        List<TopPost> topPosts,

        // Recent activity
        long newFollowersThisWeek,
        long newViewsThisWeek,
        long newLikesThisWeek

) {
    public record TopPost(
            UUID id,
            String title,
            long views,
            long likes,
            long saves,
            long comments,
            Instant publishedAt
    ) {}
}