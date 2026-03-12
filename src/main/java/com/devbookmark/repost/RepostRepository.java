package com.devbookmark.repost;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepostRepository extends JpaRepository<Repost, UUID> {

    Optional<Repost> findByUserIdAndResourceId(UUID userId, UUID resourceId);

    boolean existsByUserIdAndResourceId(UUID userId, UUID resourceId);

    // count reposts for a resource
    long countByResourceId(UUID resourceId);

    // get reposts by a user
    @EntityGraph(attributePaths = {"resource", "resource.owner",
            "resource.resourceTags", "resource.resourceTags.tag"})
    Page<Repost> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // get reposts from followed users (for feed)
    @EntityGraph(attributePaths = {"user", "resource", "resource.owner",
            "resource.resourceTags", "resource.resourceTags.tag"})
    @Query("""
        select r from Repost r
        where r.user.id in :followingIds
        order by r.createdAt desc
    """)
    Page<Repost> findByFollowingIds(
            @Param("followingIds") java.util.Set<UUID> followingIds,
            Pageable pageable
    );

    @Query("select r.resource.id from Repost r where r.user.id = :userId and r.resource.id in :resourceIds")
    List<UUID> findRepostedResourceIdsByUser(@Param("userId") UUID userId, @Param("resourceIds") List<UUID> resourceIds);

    // Batch count reposts for a list of resource IDs — eliminates N+1 in enrichList
    interface IdCountRow {
        UUID getId();
        long getCnt();
    }

    @Query("""
        select r.resource.id as id, count(r.id) as cnt
        from Repost r
        where r.resource.id in :ids
        group by r.resource.id
    """)
    List<IdCountRow> countByResourceIds(@Param("ids") List<UUID> ids);

    void deleteByResourceId(UUID resourceId);
}