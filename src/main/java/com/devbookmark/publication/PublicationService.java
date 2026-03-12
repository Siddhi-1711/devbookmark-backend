package com.devbookmark.publication;

import com.devbookmark.publication.dto.PublicationRequest;
import com.devbookmark.publication.dto.PublicationResponse;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceType;
import com.devbookmark.user.User;
import com.devbookmark.user.UserFollowRepository;
import com.devbookmark.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;
    private final UserFollowRepository followRepository;
    private final ResourceRepository resourceRepository;

    public PublicationService(PublicationRepository publicationRepository,
                              UserRepository userRepository,
                              UserFollowRepository followRepository,
                              ResourceRepository resourceRepository) {
        this.publicationRepository = publicationRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.resourceRepository = resourceRepository;
    }

    // Create publication (one per user)
    @Transactional
    public PublicationResponse create(UUID ownerId, PublicationRequest req) {
        if (publicationRepository.existsByOwnerId(ownerId)) {
            throw new IllegalArgumentException("You already have a publication.");
        }

        String slug = req.slug().trim().toLowerCase();
        if (publicationRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug already taken. Choose another.");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Publication p = Publication.builder()
                .owner(owner)
                .slug(slug)
                .name(req.name().trim())
                .bio(req.bio())
                .logoUrl(req.logoUrl())
                .build();

        Publication saved = publicationRepository.save(p);
        return toResponse(saved);
    }

    // Update publication
    @Transactional
    public PublicationResponse update(UUID ownerId, PublicationRequest req) {
        Publication p = publicationRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Publication not found."));

        String newSlug = req.slug().trim().toLowerCase();

        // check slug not taken by someone else
        if (!p.getSlug().equals(newSlug) && publicationRepository.existsBySlug(newSlug)) {
            throw new IllegalArgumentException("Slug already taken. Choose another.");
        }

        p.setSlug(newSlug);
        p.setName(req.name().trim());
        p.setBio(req.bio());
        p.setLogoUrl(req.logoUrl());

        return toResponse(p);
    }

    // Get by slug (public)
    @Transactional(readOnly = true)
    public PublicationResponse getBySlug(String slug) {
        Publication p = publicationRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Publication not found."));
        return toResponse(p);
    }

    // Get my publication
    @Transactional(readOnly = true)
    public PublicationResponse getMyPublication(UUID ownerId) {
        Publication p = publicationRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("You don't have a publication yet."));
        return toResponse(p);
    }

    // Delete publication
    @Transactional
    public void delete(UUID ownerId) {
        Publication p = publicationRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Publication not found."));
        publicationRepository.delete(p);
    }

    private PublicationResponse toResponse(Publication p) {
        long followers = followRepository.countByFolloweeId(p.getOwner().getId());
        long posts = resourceRepository.countByOwnerIdAndType(
                p.getOwner().getId(), ResourceType.WRITTEN_POST
        );

        return new PublicationResponse(
                p.getId(),
                p.getOwner().getId(),
                p.getOwner().getName(),
                p.getSlug(),
                p.getName(),
                p.getBio(),
                p.getLogoUrl(),
                p.isActive(),
                p.getCreatedAt(),
                followers,
                posts
        );
    }
}