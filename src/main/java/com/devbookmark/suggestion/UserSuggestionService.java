package com.devbookmark.suggestion;

import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import com.devbookmark.user.UserFollowRepository;
import com.devbookmark.user.dto.UserSummaryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserSuggestionService {

    private final UserRepository userRepository;
    private final UserFollowRepository followRepository;

    public UserSuggestionService(UserRepository userRepository,
                                 UserFollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    /**
     * Get user suggestions based on:
     * 1. Users followed by people I follow (2nd degree connections)
     * 2. Users with similar tags/interests
     * 3. Active users (with lots of resources)
     */
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getSuggestions(UUID me, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        Set<UUID> myFollowing = getFollowingIds(me);

        Set<UUID> secondDegree = new HashSet<>();
        for (UUID followedUser : myFollowing) {
            Set<UUID> theirFollowing = getFollowingIds(followedUser);
            secondDegree.addAll(theirFollowing);
        }
        secondDegree.removeAll(myFollowing);
        secondDegree.remove(me);

        return secondDegree.stream()
                .limit(safeLimit)
                .map(userId -> userRepository.findById(userId).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> new UserSummaryResponse(
                        u.getId(),
                        u.getName(),
                        u.getUsername(),
                        u.getAvatarUrl(),
                        followRepository.countByFolloweeId(u.getId()),
                        false
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getTrendingUsers(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
                .stream()
                .map(u -> new UserSummaryResponse(
                        u.getId(),
                        u.getName(),
                        u.getUsername(),
                        u.getAvatarUrl(),
                        followRepository.countByFolloweeId(u.getId()),
                        false
                ))
                .toList();
    }

    /**
     * Get IDs of people user follows
     */
    private Set<UUID> getFollowingIds(UUID userId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 500))
                .stream()
                .map(uf -> uf.getFollowee().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Get trending users - most active creators
     */

}