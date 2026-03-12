package com.devbookmark.user;

import com.devbookmark.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final UserRepository userRepository;
    private final UserFollowRepository followRepository;
    private final NotificationService notificationService;
    public FollowService(UserRepository userRepository, UserFollowRepository followRepository,NotificationService notificationService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.notificationService=notificationService;
    }

    @Transactional
    public void follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        if (followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId).isPresent()) return;

        User follower = userRepository.getReferenceById(followerId);
        User followee = userRepository.getReferenceById(followeeId);

        followRepository.save(UserFollow.builder()
                .follower(follower)
                .followee(followee)
                .build());
        notificationService.notifyFollow(followerId, followeeId);
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followeeId) {
        followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public Set<UUID> myFollowingIds(UUID followerId, Pageable pageable) {
        Page<UserFollow> page = followRepository.findByFollowerIdOrderByCreatedAtDesc(followerId, pageable);
        return page.getContent().stream()
                .map(f -> f.getFollowee().getId())
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public long followersCount(UUID userId) {
        return followRepository.countByFolloweeId(userId);
    }

    @Transactional(readOnly = true)
    public long followingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }
}