package com.devbookmark.admin;

import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceService;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.security.AuthUser;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import com.devbookmark.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin-only endpoints.
 * All routes under /api/admin/** are protected by ROLE_ADMIN in SecurityConfig.
 * The @PreAuthorize annotation here is a second layer of defence.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;

    public AdminController(UserRepository userRepository,
                           ResourceRepository resourceRepository,
                           ResourceService resourceService) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    /** List all users — paginated */
    @GetMapping("/users")
    public Page<AdminUserRow> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(size, 100);
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, safeSize))
                .map(u -> new AdminUserRow(
                        u.getId(), u.getName(), u.getEmail(),
                        u.getUsername(), u.getRole(), u.isBanned(),
                        u.getBanReason(), u.getCreatedAt()
                ));
    }

    /** Ban a user */
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable UUID userId,
                                     @RequestBody(required = false) Map<String, String> body,
                                     Authentication auth) {
        UUID adminId = AuthUser.requireUserId(auth);
        if (adminId.equals(userId))
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot ban yourself"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == UserRole.ADMIN)
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot ban another admin"));

        user.setBanned(true);
        user.setBanReason(body != null ? body.getOrDefault("reason", null) : null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User banned"));
    }

    /** Unban a user */
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setBanned(false);
        user.setBanReason(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User unbanned"));
    }

    /** Promote a user to ADMIN */
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User promoted to admin"));
    }

    /** Demote an admin back to USER */
    @PostMapping("/users/{userId}/demote")
    public ResponseEntity<?> demoteUser(@PathVariable UUID userId, Authentication auth) {
        UUID adminId = AuthUser.requireUserId(auth);
        if (adminId.equals(userId))
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot demote yourself"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(UserRole.USER);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User demoted to regular user"));
    }

    // ─── Resources ────────────────────────────────────────────────────────────

    /** List latest resources — admin can see all visibility levels */
    @GetMapping("/resources")
    public Page<ResourceResponse> listResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(size, 100);
        var resourcePage = resourceRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, safeSize));
        return resourceService.enrichPage(resourcePage, null);
    }

    /** Force-delete any resource regardless of owner */
    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable UUID resourceId) {
        if (!resourceRepository.existsById(resourceId))
            return ResponseEntity.notFound().build();

        // Reuse the owner's delete logic but pass the resource's own ownerId
        // so the ownership check inside ResourceService passes
        var resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        resourceService.delete(resource.getOwner().getId(), resourceId);

        return ResponseEntity.ok(Map.of("message", "Resource deleted by admin"));
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        long totalUsers     = userRepository.count();
        long bannedUsers    = userRepository.countByBannedTrue();
        long totalResources = resourceRepository.count();

        return Map.of(
                "totalUsers", totalUsers,
                "bannedUsers", bannedUsers,
                "totalResources", totalResources
        );
    }

    // ─── Row DTO ──────────────────────────────────────────────────────────────

    public record AdminUserRow(
            UUID id,
            String name,
            String email,
            String username,
            UserRole role,
            boolean banned,
            String banReason,
            java.time.Instant createdAt
    ) {}
}