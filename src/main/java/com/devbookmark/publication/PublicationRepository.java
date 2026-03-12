package com.devbookmark.publication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {

    Optional<Publication> findBySlug(String slug);

    Optional<Publication> findByOwnerId(UUID ownerId);

    boolean existsBySlug(String slug);

    boolean existsByOwnerId(UUID ownerId);
}