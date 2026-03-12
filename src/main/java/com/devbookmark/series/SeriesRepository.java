package com.devbookmark.series;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SeriesRepository extends JpaRepository<Series, UUID> {

    Optional<Series> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<Series> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<Series> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    // fetch with items eagerly
    @Query("""
        select s from Series s
        left join fetch s.items i
        left join fetch i.resource r
        left join fetch r.owner
        where s.slug = :slug
    """)
    Optional<Series> findDetailBySlug(@Param("slug") String slug);

    @Query("""
        select s from Series s
        left join fetch s.items i
        left join fetch i.resource r
        left join fetch r.owner
        where s.id = :id
    """)
    Optional<Series> findDetailById(@Param("id") UUID id);
}