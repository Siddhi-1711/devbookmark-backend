package com.devbookmark.user;

import com.devbookmark.collection.Collection;
import com.devbookmark.collection.CollectionRepository;
import com.devbookmark.collection.dto.CollectionSummaryResponse;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.user.dto.UserPublicProfileResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    public UserProfileService(UserRepository userRepository,
                              CollectionRepository collectionRepository,
                              ResourceRepository resourceRepository,
                              ResourceService resourceService) {
        this.userRepository = userRepository;
        this.collectionRepository = collectionRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
    }

    @Transactional(readOnly = true)
    public UserPublicProfileResponse publicProfile(UUID userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Collection> cols = collectionRepository.findByOwnerIdAndIsPublicTrueOrderByCreatedAtDesc(userId);

        List<CollectionSummaryResponse> colDtos = new ArrayList<>();
        for (Collection c : cols) {
            // itemCount without extra query (simple version): use c.getItems().size() only if fetched; else set -1
            long count = -1;
            colDtos.add(new CollectionSummaryResponse(
                    c.getId(),
                    c.getName(),
                    c.getDescription(),
                    c.isPublic(),
                    count,
                    c.getCreatedAt()
            ));
        }

        List<Resource> latest = resourceRepository
                .findByOwnerIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10))
                .getContent();

        // Use enrichList (batch) instead of toResponse() in a loop — fixes N+1
        List<ResourceResponse> latestDtos = resourceService.enrichList(latest, null);

        return new UserPublicProfileResponse(
                u.getId(),
                u.getName(),
                u.getUsername(),
                u.getBio(),
                u.getAvatarUrl(),
                colDtos,
                latestDtos
        );
    }
}