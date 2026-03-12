package com.devbookmark.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.devbookmark.explore.dto.TrendingResourceRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    Page<Resource> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
select new com.devbookmark.explore.dto.TrendingResourceRow(
    r.id,
    coalesce(count(distinct rl.id), 0),
    coalesce(count(distinct rs.id), 0),
    (coalesce(count(distinct rl.id), 0) * 2L + coalesce(count(distinct rs.id), 0) * 3L)
)
from Resource r
left join ResourceLike rl
    on rl.resource = r and rl.createdAt >= :since
left join ResourceSave rs
    on rs.resource = r and rs.createdAt >= :since
group by r.id
having (coalesce(count(distinct rl.id), 0) > 0 or coalesce(count(distinct rs.id), 0) > 0)
order by (coalesce(count(distinct rl.id), 0) * 2L + coalesce(count(distinct rs.id), 0) * 3L) desc
""")
    Page<TrendingResourceRow> findTrending(@Param("since") java.time.Instant since,
                                           org.springframework.data.domain.Pageable pageable);
    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    Page<Resource> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    /**
     * ✅ FIXED: Added visibility filtering parameter
     * Now respects resource visibility levels
     */
    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    @Query("""
select distinct r from Resource r
left join r.resourceTags rt
left join rt.tag t
where lower(r.title) like concat('%', lower(coalesce(:q, '')), '%')
  and (:type is null or r.type = :type)
  and (:tagsEmpty = true or lower(t.name) in :tags)
  and r.visibility in :visibilities
order by r.createdAt desc
""")
    Page<Resource> search(
            @Param("q") String q,
            @Param("type") ResourceType type,
            @Param("tagsEmpty") boolean tagsEmpty,
            @Param("tags") Set<String> tags,
            @Param("visibilities") Set<ResourceVisibility> visibilities,
            Pageable pageable
    );

    @Query("""
select distinct r.title
from Resource r
where lower(r.title) like lower(concat(:q, '%'))
order by r.title asc
""")
    List<String> findTitleSuggestions(@Param("q") String q, Pageable pageable);


    @Query("""
select distinct t.name
from Tag t
where lower(t.name) like lower(concat(:q, '%'))
order by t.name asc
""")
    List<String> findTagSuggestions(@Param("q") String q, Pageable pageable);


    org.springframework.data.domain.Page<Resource> findByOwnerIdInOrderByCreatedAtDesc(
            java.util.Set<java.util.UUID> ownerIds,
            org.springframework.data.domain.Pageable pageable
    );
    long countByOwnerId(UUID ownerId);

    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    @Query("""
select distinct r from Resource r
join fetch r.owner o
left join fetch r.resourceTags rt
left join fetch rt.tag t
where o.id = :userId
order by r.createdAt desc
""")
    List<Resource> findProfileResources(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
select new com.devbookmark.explore.dto.TrendingResourceRow(
    r.id,
    coalesce(count(distinct rl.id), 0),
    coalesce(count(distinct rs.id), 0),
    (coalesce(count(distinct rl.id), 0) * 2L + coalesce(count(distinct rs.id), 0) * 3L)
)
from Resource r
left join ResourceLike rl on rl.resource = r
left join ResourceSave rs on rs.resource = r
group by r.id
having (coalesce(count(distinct rl.id), 0) > 0 or coalesce(count(distinct rs.id), 0) > 0)
order by (coalesce(count(distinct rl.id), 0) * 2L + coalesce(count(distinct rs.id), 0) * 3L) desc
""")
    Page<TrendingResourceRow> findPopular(Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    Page<Resource> findByVisibilityOrderByCreatedAtDesc(ResourceVisibility visibility, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    Page<Resource> findByVisibilityInOrderByCreatedAtDesc(Set<ResourceVisibility> visibilities, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    @Query("""
select distinct r
from Resource r
where (r.owner.id = :me)
   or (r.owner.id in :following and r.visibility in :visibleToFollower)
order by r.createdAt desc
""")
    Page<Resource> findFeed(
            @Param("me") UUID me,
            @Param("following") Set<UUID> following,
            @Param("visibleToFollower") Set<ResourceVisibility> visibleToFollower,
            Pageable pageable
    );

    long countByOwnerIdAndType(UUID ownerId, ResourceType type);

    long countByOwnerIdAndTypeAndIsPublished(UUID ownerId, ResourceType type, boolean isPublished);

    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    @Query("""
    select distinct r
    from Resource r
    left join r.resourceTags rt
    left join rt.tag t
    where (
        r.owner.id = :me
        or (r.owner.id in :following and r.visibility in :visibleToFollower)
        or (lower(t.name) in :followedTags and r.visibility = com.devbookmark.resource.ResourceVisibility.PUBLIC)
    )
    order by r.createdAt desc
""")
    Page<Resource> findFeedWithTags(
            @Param("me") UUID me,
            @Param("following") Set<UUID> following,
            @Param("followedTags") List<String> followedTags,
            @Param("visibleToFollower") Set<ResourceVisibility> visibleToFollower,
            Pageable pageable
    );

    // Advanced search with author, date range, published filter

    // Author name suggestions
    @Query("""
    select distinct u.name from Resource r
    join r.owner u
    where lower(u.name) like lower(concat('%', :q, '%'))
      and r.visibility = com.devbookmark.resource.ResourceVisibility.PUBLIC
""")
    List<String> findAuthorSuggestions(@Param("q") String q, Pageable pageable);
    @EntityGraph(attributePaths = {"owner", "resourceTags", "resourceTags.tag"})
    @Query("""
    select distinct r from Resource r
    join r.resourceTags rt
    join rt.tag t
    where lower(t.name) in :tagNames
      and r.visibility = com.devbookmark.resource.ResourceVisibility.PUBLIC
    order by r.createdAt desc
""")
    Page<Resource> findByTagNamesOrderByCreatedAtDesc(
            @Param("tagNames") List<String> tagNames,
            Pageable pageable
    );

}