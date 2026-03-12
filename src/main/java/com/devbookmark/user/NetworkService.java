package com.devbookmark.user;

import com.devbookmark.user.dto.UserSummaryResponse;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NetworkService {

    private final UserFollowRepository repo;

    public NetworkService(UserFollowRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> myFollowing(UUID me, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size));
        return repo.findByFollowerIdOrderByCreatedAtDesc(me, pageable)
                .map(uf -> toSummary(uf.getFollowee(), me));
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> myFollowers(UUID me, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size));
        return repo.findByFolloweeIdOrderByCreatedAtDesc(me, pageable)
                .map(uf -> toSummary(uf.getFollower(), me));
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> userFollowing(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size));
        return repo.findByFollowerIdOrderByCreatedAtDesc(userId, pageable)
                .map(uf -> toSummary(uf.getFollowee(), null));
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> userFollowers(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size));
        return repo.findByFolloweeIdOrderByCreatedAtDesc(userId, pageable)
                .map(uf -> toSummary(uf.getFollower(), null));
    }

    private UserSummaryResponse toSummary(User u, UUID viewerId) {
        long followers = repo.countByFolloweeId(u.getId());
        boolean followedByMe = viewerId != null &&
                repo.existsByFollowerIdAndFolloweeId(viewerId, u.getId());
        return new UserSummaryResponse(u.getId(), u.getName(), u.getUsername(), u.getAvatarUrl(), followers, followedByMe);
    }

    private int clamp(int s) {
        return Math.min(Math.max(s, 1), 50);
    }
}