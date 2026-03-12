package com.devbookmark.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResourceLikeRepository extends JpaRepository<ResourceLike, UUID> {
    Optional<ResourceLike> findByUserIdAndResourceId(UUID userId, UUID resourceId);
    long countByResourceId(UUID resourceId);

    interface IdCountRow {
        UUID getId();
        long getCnt();
    }

    @Query("""
        select rl.resource.id as id, count(rl.id) as cnt
        from ResourceLike rl
        where rl.resource.id in :ids
        group by rl.resource.id
    """)
    List<IdCountRow> countByResourceIds(@Param("ids") List<UUID> ids);

    @Query("""
        select rl.resource.id
        from ResourceLike rl
        where rl.user.id = :userId and rl.resource.id in :ids
    """)
    List<UUID> findLikedResourceIdsByUser(@Param("userId") UUID userId, @Param("ids") List<UUID> ids);

    /**
     * ✅ NEW: Batch delete for bulk unlike operations
     */
    long deleteByUserIdAndResourceIdIn(@Param("userId") UUID userId, @Param("resourceIds") Set<UUID> resourceIds);

    @Query("select count(rl) from ResourceLike rl where rl.resource.owner.id = :ownerId")
    long countByResourceOwnerId(@Param("ownerId") UUID ownerId);

    @Query("select count(rl) from ResourceLike rl where rl.resource.owner.id = :ownerId and rl.createdAt >= :since")
    long countByResourceOwnerIdAndCreatedAtAfter(@Param("ownerId") UUID ownerId, @Param("since") Instant since);
}