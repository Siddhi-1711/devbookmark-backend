package com.devbookmark.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    Optional<UserFollow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    long countByFolloweeId(UUID userId); // followers
    long countByFollowerId(UUID userId); // following

    Page<UserFollow> findByFollowerIdOrderByCreatedAtDesc(UUID followerId, Pageable pageable);
    Page<UserFollow> findByFolloweeIdOrderByCreatedAtDesc(UUID followeeId, Pageable pageable);

    long countByFolloweeIdAndCreatedAtAfter(UUID followeeId, Instant since);
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
}