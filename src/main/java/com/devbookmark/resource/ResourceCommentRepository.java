package com.devbookmark.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ResourceCommentRepository extends JpaRepository<ResourceComment, UUID> {

    // top level comments only (no replies)
    @EntityGraph(attributePaths = {"user", "replies", "replies.user"})
    @Query("""
        select c from ResourceComment c
        where c.resource.id = :resourceId
          and c.parent is null
        order by c.createdAt desc
    """)
    Page<ResourceComment> findTopLevelComments(
            @Param("resourceId") UUID resourceId,
            Pageable pageable
    );

    // count all comments including replies
    long countByResourceId(UUID resourceId);

    // count by owner
    @Query("select count(c) from ResourceComment c where c.resource.owner.id = :ownerId")
    long countByResourceOwnerId(@Param("ownerId") UUID ownerId);
}