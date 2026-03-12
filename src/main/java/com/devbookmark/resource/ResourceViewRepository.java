package com.devbookmark.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ResourceViewRepository extends JpaRepository<ResourceView, UUID> {

    // total views for a resource
    long countByResourceId(UUID resourceId);

    // unique user views
    long countByResourceIdAndUserIsNotNull(UUID resourceId);

    // check if user viewed today (for unique view logic)
    @Query("""
        select count(v) > 0 from ResourceView v
        where v.resource.id = :resourceId
          and v.user.id = :userId
          and v.createdAt >= :since
    """)
    boolean existsRecentView(
            @Param("resourceId") UUID resourceId,
            @Param("userId") UUID userId,
            @Param("since") Instant since
    );

    // check if ip viewed today (for anonymous)
    @Query("""
        select count(v) > 0 from ResourceView v
        where v.resource.id = :resourceId
          and v.ipAddress = :ip
          and v.createdAt >= :since
    """)
    boolean existsRecentViewByIp(
            @Param("resourceId") UUID resourceId,
            @Param("ip") String ip,
            @Param("since") Instant since
    );

    // total reads completed
    long countByResourceIdAndReadCompletedTrue(UUID resourceId);

    // views by owner (for creator stats)
    @Query("""
        select count(v) from ResourceView v
        where v.resource.owner.id = :ownerId
    """)
    long countTotalViewsByOwner(@Param("ownerId") UUID ownerId);

    // find today's view by user (for marking read completed)
    @Query("""
    select v from ResourceView v
    where v.resource.id = :resourceId
      and v.user.id = :userId
      and v.createdAt >= :since
    order by v.createdAt desc
""")
    java.util.Optional<ResourceView> findRecentViewByUser(
            @Param("resourceId") UUID resourceId,
            @Param("userId") UUID userId,
            @Param("since") Instant since
    );
    @Query("select count(v) from ResourceView v where v.resource.owner.id = :ownerId and v.createdAt >= :since")
    long countByResourceOwnerIdAndCreatedAtAfter(@Param("ownerId") UUID ownerId, @Param("since") Instant since);

    void deleteByResourceId(UUID resourceId);

    // Batch count views for a list of resource IDs — eliminates N+1 in enrichList
    interface IdCountRow {
        UUID getId();
        long getCnt();
    }

    @Query("""
        select v.resource.id as id, count(v.id) as cnt
        from ResourceView v
        where v.resource.id in :ids
        group by v.resource.id
    """)
    List<IdCountRow> countByResourceIds(@Param("ids") List<UUID> ids);
}