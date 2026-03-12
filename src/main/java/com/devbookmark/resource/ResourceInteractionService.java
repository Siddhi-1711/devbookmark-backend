package com.devbookmark.resource;

import com.devbookmark.activity.ActivityService;
import com.devbookmark.notification.NotificationService;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ResourceInteractionService {

    private final ResourceRepository resourceRepository;
    private final ResourceLikeRepository likeRepository;
    private final ResourceSaveRepository saveRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityService activityService; // ADD
    public ResourceInteractionService(ResourceRepository resourceRepository,
                                      ResourceLikeRepository likeRepository,
                                      ResourceSaveRepository saveRepository,
                                      UserRepository userRepository,
                                      NotificationService notificationService,
                                      ActivityService activityService) {
        this.resourceRepository = resourceRepository;
        this.likeRepository = likeRepository;
        this.saveRepository = saveRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityService = activityService;
    }

    @Transactional
    public void like(UUID userId, UUID resourceId) {
        if (likeRepository.findByUserIdAndResourceId(userId, resourceId).isPresent()) return;

        User user = userRepository.getReferenceById(userId);
        Resource resource = resourceRepository.getReferenceById(resourceId);

        likeRepository.save(ResourceLike.builder()
                .user(user)
                .resource(resource)
                .build());
        activityService.logActivityLike(userId, resource);
        notificationService.notifyLike(userId, resourceId);
    }

    @Transactional
    public void unlike(UUID userId, UUID resourceId) {
        likeRepository.findByUserIdAndResourceId(userId, resourceId)
                .ifPresent(likeRepository::delete);
    }

    @Transactional
    public void save(UUID userId, UUID resourceId) {
        if (saveRepository.findByUserIdAndResourceId(userId, resourceId).isPresent()) return;

        User user = userRepository.getReferenceById(userId);
        Resource resource = resourceRepository.getReferenceById(resourceId);

        saveRepository.save(ResourceSave.builder()
                .user(user)
                .resource(resource)
                .build());
        activityService.logActivitySave(userId, resource);
        notificationService.notifySave(userId, resourceId);
    }

    @Transactional
    public void unsave(UUID userId, UUID resourceId) {
        saveRepository.findByUserIdAndResourceId(userId, resourceId)
                .ifPresent(saveRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<Resource> mySaved(UUID userId) {
        List<ResourceSave> saves = saveRepository.findSavedWithResourceOwnerAndTags(userId);
        List<Resource> list = new ArrayList<>();
        for (ResourceSave s : saves) {
            list.add(s.getResource());
        }
        return list;
    }
}