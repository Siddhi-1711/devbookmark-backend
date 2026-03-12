package com.devbookmark.pin;

import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PinService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    public PinService(UserRepository userRepository,
                      ResourceRepository resourceRepository,
                      ResourceService resourceService) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
    }

    // Pin a resource to slot 1, 2, or 3
    @Transactional
    public void pin(UUID userId, UUID resourceId, int slot) {
        if (slot < 1 || slot > 3) {
            throw new IllegalArgumentException("Slot must be 1, 2, or 3.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // verify resource belongs to user
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        if (!resource.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only pin your own resources.");
        }

// ADD THIS - only allow pinning public resources
        if (resource.getVisibility() == com.devbookmark.resource.ResourceVisibility.PRIVATE) {
            throw new IllegalArgumentException("Cannot pin a private resource.");
        }
        switch (slot) {
            case 1 -> user.setPinnedResource1(resourceId);
            case 2 -> user.setPinnedResource2(resourceId);
            case 3 -> user.setPinnedResource3(resourceId);
        }

        userRepository.save(user);
    }

    // Unpin a slot
    @Transactional
    public void unpin(UUID userId, int slot) {
        if (slot < 1 || slot > 3) {
            throw new IllegalArgumentException("Slot must be 1, 2, or 3.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        switch (slot) {
            case 1 -> user.setPinnedResource1(null);
            case 2 -> user.setPinnedResource2(null);
            case 3 -> user.setPinnedResource3(null);
        }

        userRepository.save(user);
    }

    // Get pinned resources for a user
    @Transactional(readOnly = true)
    public List<ResourceResponse> getPinnedResources(UUID userId) {
        return getPinnedResources(userId, null);
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getPinnedResources(UUID userId, UUID viewerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        List<ResourceResponse> pinned = new ArrayList<>();
        boolean isOwner = userId.equals(viewerId);

        addIfExists(user.getPinnedResource1(), pinned, isOwner);
        addIfExists(user.getPinnedResource2(), pinned, isOwner);
        addIfExists(user.getPinnedResource3(), pinned, isOwner);

        return pinned;
    }

    private void addIfExists(UUID resourceId, List<ResourceResponse> list, boolean isOwner) {
        if (resourceId == null) return;
        resourceRepository.findById(resourceId).ifPresent(r -> {
            // owner sees all, public sees PUBLIC and FOLLOWERS pinned resources
            if (isOwner ||
                    r.getVisibility() == com.devbookmark.resource.ResourceVisibility.PUBLIC ||
                    r.getVisibility() == com.devbookmark.resource.ResourceVisibility.FOLLOWERS) {
                list.add(resourceService.toResponse(r));
            }
        });
    }
    private void addIfExists(UUID resourceId, List<ResourceResponse> list) {
        if (resourceId == null) return;
        resourceRepository.findById(resourceId)
                .ifPresent(r -> {
                    // only show PUBLIC resources in public pins
                    if (r.getVisibility() == com.devbookmark.resource.ResourceVisibility.PUBLIC) {
                        list.add(resourceService.toResponse(r));
                    }
                });
    }
}