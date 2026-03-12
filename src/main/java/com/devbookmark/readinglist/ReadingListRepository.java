package com.devbookmark.readinglist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReadingListRepository extends JpaRepository<ReadingListItem, UUID> {

    Optional<ReadingListItem> findByUserIdAndResourceId(UUID userId, UUID resourceId);

    boolean existsByUserIdAndResourceId(UUID userId, UUID resourceId);

    // unread items
    @Query("""
        select r from ReadingListItem r
        join fetch r.resource res
        join fetch res.owner
        left join fetch res.resourceTags rt
        left join fetch rt.tag
        where r.user.id = :userId
          and r.isRead = false
        order by r.addedAt desc
    """)
    Page<ReadingListItem> findUnread(@Param("userId") UUID userId, Pageable pageable);

    // all items
    @Query("""
        select r from ReadingListItem r
        join fetch r.resource res
        join fetch res.owner
        left join fetch res.resourceTags rt
        left join fetch rt.tag
        where r.user.id = :userId
        order by r.addedAt desc
    """)
    Page<ReadingListItem> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    long countByUserIdAndIsRead(UUID userId, boolean isRead);
    void deleteByResourceId(UUID resourceId);
}