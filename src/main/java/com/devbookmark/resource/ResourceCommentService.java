package com.devbookmark.resource;

import com.devbookmark.notification.NotificationService;
import com.devbookmark.resource.dto.*;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceCommentService {

    private final ResourceRepository resourceRepository;
    private final ResourceCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ResourceCommentService(ResourceRepository resourceRepository,
                                  ResourceCommentRepository commentRepository,
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.resourceRepository = resourceRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public CommentResponse add(UUID me, UUID resourceId, CommentCreateRequest req) {
        Resource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        User u = userRepository.getReferenceById(me);

        // handle reply
        ResourceComment parent = null;
        if (req.parentId() != null) {
            parent = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found."));
        }

        ResourceComment saved = commentRepository.saveAndFlush(
                ResourceComment.builder()
                        .resource(r)
                        .user(u)
                        .text(req.text())
                        .parent(parent)
                        .build()
        );

        notificationService.notifyComment(me, resourceId, saved.getId());
        return toResponse(saved, me);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> list(UUID me, UUID resourceId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        Page<ResourceComment> p = commentRepository.findTopLevelComments(
                resourceId, PageRequest.of(safePage, safeSize)
        );

        return p.map(c -> toResponse(c, me));
    }

    @Transactional
    public CommentResponse update(UUID me, UUID commentId, CommentUpdateRequest req) {
        ResourceComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));

        if (!c.getUser().getId().equals(me)) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        }

        c.setText(req.text());
        return toResponse(c, me);
    }

    @Transactional
    public void delete(UUID me, UUID commentId) {
        ResourceComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found."));

        boolean isOwner = c.getUser().getId().equals(me);
        boolean isResourceOwner = c.getResource().getOwner().getId().equals(me);

        if (!isOwner && !isResourceOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        }

        commentRepository.delete(c);
    }

    private CommentResponse toResponse(ResourceComment c, UUID me) {
        // map replies
        List<CommentResponse> replies = c.getReplies()
                .stream()
                .map(r -> toResponse(r, me))
                .toList();

        return new CommentResponse(
                c.getId(),
                c.getUser().getId(),
                c.getUser().getName(),
                c.getText(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                me != null && c.getUser().getId().equals(me),
                c.getParent() != null ? c.getParent().getId() : null,
                replies,
                replies.size()
        );
    }
}