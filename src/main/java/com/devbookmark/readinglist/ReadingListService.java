package com.devbookmark.readinglist;

import com.devbookmark.readinglist.dto.ReadingListResponse;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReadingListService {

    private final ReadingListRepository readingListRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final UserRepository userRepository;

    public ReadingListService(ReadingListRepository readingListRepository,
                              ResourceRepository resourceRepository,
                              ResourceService resourceService,
                              UserRepository userRepository) {
        this.readingListRepository = readingListRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.userRepository = userRepository;
    }

    // Add to reading list
    @Transactional
    public void add(UUID userId, UUID resourceId) {
        if (readingListRepository.existsByUserIdAndResourceId(userId, resourceId)) {
            return; // idempotent
        }

        User user = userRepository.getReferenceById(userId);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        readingListRepository.save(ReadingListItem.builder()
                .user(user)
                .resource(resource)
                .build());
    }

    // Remove from reading list
    @Transactional
    public void remove(UUID userId, UUID resourceId) {
        readingListRepository.findByUserIdAndResourceId(userId, resourceId)
                .ifPresent(readingListRepository::delete);
    }

    // Mark as read
    @Transactional
    public void markAsRead(UUID userId, UUID resourceId) {
        ReadingListItem item = readingListRepository
                .findByUserIdAndResourceId(userId, resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Item not in reading list."));

        item.setRead(true);
        item.setReadAt(Instant.now());
        readingListRepository.save(item);
    }

    // Mark as unread
    @Transactional
    public void markAsUnread(UUID userId, UUID resourceId) {
        ReadingListItem item = readingListRepository
                .findByUserIdAndResourceId(userId, resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Item not in reading list."));

        item.setRead(false);
        item.setReadAt(null);
        readingListRepository.save(item);
    }

    // Get all reading list items
    @Transactional(readOnly = true)
    public Page<ReadingListResponse> getAll(UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        return readingListRepository
                .findAllByUserId(userId, PageRequest.of(safePage, safeSize))
                .map(item -> toResponse(item, userId));
    }

    // Get unread items only
    @Transactional(readOnly = true)
    public Page<ReadingListResponse> getUnread(UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        return readingListRepository
                .findUnread(userId, PageRequest.of(safePage, safeSize))
                .map(item -> toResponse(item, userId));
    }

    // Get counts
    @Transactional(readOnly = true)
    public ReadingListStats getStats(UUID userId) {
        long unread = readingListRepository.countByUserIdAndIsRead(userId, false);
        long read = readingListRepository.countByUserIdAndIsRead(userId, true);
        return new ReadingListStats(unread, read, unread + read);
    }

    private ReadingListResponse toResponse(ReadingListItem item, UUID userId) {
        return new ReadingListResponse(
                item.getId(),
                resourceService.toResponse(item.getResource()),
                item.isRead(),
                item.getReadAt(),
                item.getAddedAt()
        );
    }

    public record ReadingListStats(
            long unread,
            long read,
            long total
    ) {}

}