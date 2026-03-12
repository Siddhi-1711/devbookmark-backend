package com.devbookmark.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResourceSaveRepository extends JpaRepository<ResourceSave, UUID> {
    Page<ResourceSave> findByUserId(UUID userId, Pageable pageable);
    Optional<ResourceSave> findByUserIdAndResourceId(UUID userId, UUID resourceId);

    long countByResourceId(UUID resourceId);

    interface IdCountRow {
        UUID getId();
        long getCnt();
    }

    @Query("""
        select rs.resource.id as id, count(rs.id) as cnt
        from ResourceSave rs
        where rs.resource.id in :ids
        group by rs.resource.id
    """)
    List<IdCountRow> countByResourceIds(@Param("ids") List<UUID> ids);

    @Query("""
        select rs.resource.id
        from ResourceSave rs
        where rs.user.id = :userId and rs.resource.id in :ids
    """)
    List<UUID> findSavedResourceIdsByUser(@Param("userId") UUID userId, @Param("ids") List<UUID> ids);

    @Query("""
        select rs from ResourceSave rs
        join fetch rs.resource r
        join fetch r.owner o
        left join fetch r.resourceTags rt
        left join fetch rt.tag t
        where rs.user.id = :userId
        order by rs.createdAt desc
    """)
    List<ResourceSave> findSavedWithResourceOwnerAndTags(UUID userId);

    /**
     * ✅ NEW: Batch delete for bulk unsave operations
     */
    long deleteByUserIdAndResourceIdIn(@Param("userId") UUID userId, @Param("resourceIds") Set<UUID> resourceIds);

    @Query("select count(rs) from ResourceSave rs where rs.resource.owner.id = :ownerId")
    long countByResourceOwnerId(@Param("ownerId") UUID ownerId);
}