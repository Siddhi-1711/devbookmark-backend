package com.devbookmark.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ResourceTagRepository extends JpaRepository<ResourceTag, UUID> {

    // trending tags - most used in last N days
    @Query("""
        select t.name, count(rt.id) as usageCount
        from ResourceTag rt
        join rt.tag t
        join rt.resource r
        where r.createdAt >= :since
          and r.visibility = com.devbookmark.resource.ResourceVisibility.PUBLIC
        group by t.name
        order by count(rt.id) desc
    """)
    List<Object[]> findTrendingTags(@Param("since") Instant since,
                                    org.springframework.data.domain.Pageable pageable);

    // all time popular tags
    @Query("""
        select t.name, count(rt.id) as usageCount
        from ResourceTag rt
        join rt.tag t
        join rt.resource r
        where r.visibility = com.devbookmark.resource.ResourceVisibility.PUBLIC
        group by t.name
        order by count(rt.id) desc
    """)
    List<Object[]> findPopularTags(org.springframework.data.domain.Pageable pageable);
}