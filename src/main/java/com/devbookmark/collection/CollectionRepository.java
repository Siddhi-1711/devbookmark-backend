package com.devbookmark.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    List<Collection> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    // Used for visibility rule: owner can view even if private
    Optional<Collection> findByIdAndOwnerId(UUID id, UUID ownerId);

    @org.springframework.data.jpa.repository.Query("""
select c from Collection c
left join fetch c.items i
left join fetch i.resource r
left join fetch r.resourceTags rt
left join fetch rt.tag t
where c.id = :id
""")
    java.util.Optional<Collection> findDetailById(java.util.UUID id);
    java.util.List<Collection> findByOwnerIdAndIsPublicTrueOrderByCreatedAtDesc(java.util.UUID ownerId);
}