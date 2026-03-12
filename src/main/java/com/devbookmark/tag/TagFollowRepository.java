package com.devbookmark.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagFollowRepository extends JpaRepository<TagFollow, UUID> {

    Optional<TagFollow> findByUserIdAndTagId(UUID userId, UUID tagId);

    boolean existsByUserIdAndTagId(UUID userId, UUID tagId);

    // get all tag names user follows
    @Query("""
        select tf.tag.name
        from TagFollow tf
        where tf.user.id = :userId
    """)
    List<String> findFollowedTagNamesByUserId(@Param("userId") UUID userId);

    // get all tag ids user follows
    @Query("""
        select tf.tag.id
        from TagFollow tf
        where tf.user.id = :userId
    """)
    List<UUID> findFollowedTagIdsByUserId(@Param("userId") UUID userId);

    // count followers of a tag
    long countByTagId(UUID tagId);

    void deleteByUserIdAndTagId(UUID userId, UUID tagId);
}