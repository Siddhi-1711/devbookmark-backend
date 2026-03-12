package com.devbookmark.notification;

import com.devbookmark.notification.dto.MarkReadRequest;
import com.devbookmark.notification.dto.NotificationResponse;
import com.devbookmark.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Page<NotificationResponse> myNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return notificationService.myNotifications(me, page, size);
    }

    @GetMapping("/unread-count")
    public long unreadCount(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return notificationService.unreadCount(me);
    }

    @PostMapping("/read")
    public void markRead(Authentication auth, @Valid @RequestBody MarkReadRequest req) {
        UUID me = AuthUser.requireUserId(auth);
        notificationService.markRead(me, req.ids());
    }
    @PostMapping("/read-all")
    public void markAllRead(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        notificationService.markAllRead(me);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable UUID id) {
        UUID me = AuthUser.requireUserId(auth);
        notificationService.delete(me, id);
    }

}