package com.devbookmark.notification;

import com.devbookmark.notification.dto.NotificationResponse;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               ResourceRepository resourceRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    // ---- creators (called from other services) ----

    @Transactional
    public void notifyFollow(UUID actorId, UUID recipientId) {
        if (actorId.equals(recipientId)) return;

        User actor = userRepository.getReferenceById(actorId);
        User recipient = userRepository.getReferenceById(recipientId);

        notificationRepository.save(Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(NotificationType.FOLLOWED_YOU)
                .build());
    }

    @Transactional
    public void notifyLike(UUID actorId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) return;

        UUID recipientId = resource.getOwner().getId();
        if (actorId.equals(recipientId)) return;

        User actor = userRepository.getReferenceById(actorId);
        User recipient = userRepository.getReferenceById(recipientId);

        notificationRepository.save(Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(NotificationType.LIKED_RESOURCE)
                .resource(resource)
                .build());
    }

    @Transactional
    public void notifySave(UUID actorId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) return;

        UUID recipientId = resource.getOwner().getId();
        if (actorId.equals(recipientId)) return;

        User actor = userRepository.getReferenceById(actorId);
        User recipient = userRepository.getReferenceById(recipientId);

        notificationRepository.save(Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(NotificationType.SAVED_RESOURCE)
                .resource(resource)
                .build());
    }

    @Transactional
    public void notifyComment(UUID actorId, UUID resourceId, UUID commentId) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) return;

        UUID recipientId = resource.getOwner().getId();

        // do not notify yourself
        if (actorId.equals(recipientId)) return;

        User actor = userRepository.getReferenceById(actorId);
        User recipient = userRepository.getReferenceById(recipientId);

        notificationRepository.save(Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(NotificationType.COMMENTED_RESOURCE)
                .resource(resource)
                .build());
    }

    // ---- user APIs ----

    @Transactional(readOnly = true)
    public Page<NotificationResponse> myNotifications(UUID me, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        var result = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                me, PageRequest.of(safePage, safeSize)
        );

        return result.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID me) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(me);
    }

    @Transactional
    public void markRead(UUID me, Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) return;

        List<Notification> list = notificationRepository.findAllById(ids);
        Instant now = Instant.now();
        boolean anyUpdated = false;

        for (Notification n : list) {
            if (!n.getRecipient().getId().equals(me)) continue;
            if (n.getReadAt() == null) {
                n.setReadAt(now);
                anyUpdated = true;
            }
        }

        // ✅ FIXED: Explicit save, not relying on dirty checking
        if (anyUpdated) {
            notificationRepository.saveAll(list);
        }
    }

    @Transactional
    public void markAllRead(UUID me) {
        notificationRepository.markAllRead(me, Instant.now());
    }

    @Transactional
    public void delete(UUID me, UUID notificationId) {
        notificationRepository.deleteByIdAndRecipientId(notificationId, me);
    }

    private NotificationResponse toResponse(Notification n) {
        UUID resourceId = (n.getResource() == null) ? null : n.getResource().getId();
        String resourceTitle = (n.getResource() == null) ? null : n.getResource().getTitle();

        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getActor().getId(),
                n.getActor().getName(),
                resourceId,
                resourceTitle,
                n.getReadAt() != null,
                n.getCreatedAt()
        );
    }

    @Transactional
    public void notifyRepost(UUID actorId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) return;

        UUID recipientId = resource.getOwner().getId();
        if (actorId.equals(recipientId)) return;

        User actor = userRepository.getReferenceById(actorId);
        User recipient = userRepository.getReferenceById(recipientId);

        notificationRepository.save(Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(NotificationType.REPOSTED_RESOURCE)
                .resource(resource)
                .build());
    }
}