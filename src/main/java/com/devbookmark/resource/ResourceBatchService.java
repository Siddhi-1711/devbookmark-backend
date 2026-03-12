package com.devbookmark.resource;

import com.devbookmark.notification.NotificationService;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ResourceBatchService {

    private final ResourceRepository resourceRepository;
    private final ResourceLikeRepository likeRepository;
    private final ResourceSaveRepository saveRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ResourceBatchService(ResourceRepository resourceRepository,
                                ResourceLikeRepository likeRepository,
                                ResourceSaveRepository saveRepository,
                                UserRepository userRepository,
                                NotificationService notificationService) {
        this.resourceRepository = resourceRepository;
        this.likeRepository = likeRepository;
        this.saveRepository = saveRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Like multiple resources at once
     */
    @Transactional
    public void batchLike(UUID userId, Set<UUID> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        for (UUID resourceId : resourceIds) {
            // Skip if already liked
            if (likeRepository.findByUserIdAndResourceId(userId, resourceId).isPresent()) {
                continue;
            }

            Resource resource = resourceRepository.findById(resourceId).orElse(null);
            if (resource == null) continue;

            likeRepository.save(ResourceLike.builder()
                    .user(user)
                    .resource(resource)
                    .build());

            notificationService.notifyLike(userId, resourceId);
        }
    }

    /**
     * Save multiple resources at once
     */
    @Transactional
    public void batchSave(UUID userId, Set<UUID> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        for (UUID resourceId : resourceIds) {
            // Skip if already saved
            if (saveRepository.findByUserIdAndResourceId(userId, resourceId).isPresent()) {
                continue;
            }

            Resource resource = resourceRepository.findById(resourceId).orElse(null);
            if (resource == null) continue;

            saveRepository.save(ResourceSave.builder()
                    .user(user)
                    .resource(resource)
                    .build());

            notificationService.notifySave(userId, resourceId);
        }
    }

    /**
     * Unlike multiple resources
     */
    @Transactional
    public void batchUnlike(UUID userId, Set<UUID> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) return;

        likeRepository.deleteByUserIdAndResourceIdIn(userId, resourceIds);
    }

    /**
     * Unsave multiple resources
     */
    @Transactional
    public void batchUnsave(UUID userId, Set<UUID> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) return;

        saveRepository.deleteByUserIdAndResourceIdIn(userId, resourceIds);
    }

    /**
     * Delete multiple resources (only owner)
     */
    @Transactional
    public void batchDelete(UUID userId, Set<UUID> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) return;

        List<Resource> toDelete = resourceRepository.findAllById(resourceIds);

        for (Resource r : toDelete) {
            if (r.getOwner().getId().equals(userId)) {
                resourceRepository.delete(r);
            }
        }
    }
}