package com.devbookmark.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<UserFollow, UUID> {

    // users I follow
    @Query("""
        select f.followee
        from UserFollow f
        where f.follower.id = :me
        order by f.createdAt desc
    """)
    Page<User> findFollowing(@Param("me") UUID me, Pageable pageable);

    // users who follow me
    @Query("""
        select f.follower
        from UserFollow f
        where f.followee.id = :me
        order by f.createdAt desc
    """)
    Page<User> findFollowers(@Param("me") UUID me, Pageable pageable);
}