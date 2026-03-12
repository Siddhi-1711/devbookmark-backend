package com.devbookmark.readinglist;

import com.devbookmark.readinglist.dto.ReadingListResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reading-list")
public class ReadingListController {

    private final ReadingListService readingListService;

    public ReadingListController(ReadingListService readingListService) {
        this.readingListService = readingListService;
    }

    // Add to reading list
    @PostMapping("/{resourceId}")
    public void add(Authentication auth, @PathVariable UUID resourceId) {
        UUID me = AuthUser.requireUserId(auth);
        readingListService.add(me, resourceId);
    }

    // Remove from reading list
    @DeleteMapping("/{resourceId}")
    public void remove(Authentication auth, @PathVariable UUID resourceId) {
        UUID me = AuthUser.requireUserId(auth);
        readingListService.remove(me, resourceId);
    }

    // Mark as read
    @PostMapping("/{resourceId}/read")
    public void markAsRead(Authentication auth, @PathVariable UUID resourceId) {
        UUID me = AuthUser.requireUserId(auth);
        readingListService.markAsRead(me, resourceId);
    }

    // Mark as unread
    @PostMapping("/{resourceId}/unread")
    public void markAsUnread(Authentication auth, @PathVariable UUID resourceId) {
        UUID me = AuthUser.requireUserId(auth);
        readingListService.markAsUnread(me, resourceId);
    }

    // Get all items
    @GetMapping
    public Page<ReadingListResponse> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return readingListService.getAll(me, page, size);
    }

    // Get unread only
    @GetMapping("/unread")
    public Page<ReadingListResponse> getUnread(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return readingListService.getUnread(me, page, size);
    }

    // Get stats
    @GetMapping("/stats")
    public ReadingListService.ReadingListStats getStats(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return readingListService.getStats(me);
    }
}