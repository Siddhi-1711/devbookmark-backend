package com.devbookmark.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionResourceRepository extends JpaRepository<CollectionResource, UUID> {
    Optional<CollectionResource> findByCollectionIdAndResourceId(UUID collectionId, UUID resourceId);
    long countByCollectionId(UUID collectionId);
}