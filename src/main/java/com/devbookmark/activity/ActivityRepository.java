package com.devbookmark.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    @EntityGraph(attributePaths = {"user", "resource", "resource.owner", "resource.resourceTags", "resource.resourceTags.tag"})
    @Query("""
        select a from Activity a
        where a.user.id in :followingIds
          and a.type in :types
          and a.resource is not null
        order by a.createdAt desc
    """)
    Page<Activity> findFollowingActivities(
            @Param("followingIds") Set<UUID> followingIds,
            @Param("types") Set<ActivityType> types,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "resource", "resource.owner", "resource.resourceTags", "resource.resourceTags.tag"})
    Page<Activity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);
    @Modifying
    @Query("update Activity a set a.resource = null where a.resource.id = :resourceId")
    void nullifyResource(@Param("resourceId") UUID resourceId);
}