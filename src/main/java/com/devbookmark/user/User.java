package com.devbookmark.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        })
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ─── Role ─────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserRole role = UserRole.USER;

    // ─── Ban support ──────────────────────────────────────────────────────────
    @Column(name = "is_banned", nullable = false)
    @Builder.Default
    private boolean banned = false;

    @Column(name = "ban_reason", length = 300)
    private String banReason;

    // ─── Profile fields ──────────────────────────────────────────────────────

    /** Unique handle e.g. "siddhi_dev" — optional, set by user in settings */
    @Column(name = "username", length = 40)
    private String username;

    /** Short bio shown on profile page */
    @Column(name = "bio", length = 300)
    private String bio;

    /** Cloudinary / external URL for profile picture */
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    // ─── Pinned resources (max 3) ─────────────────────────────────────────────
    @Column(name = "pinned_resource_1")
    private UUID pinnedResource1;

    @Column(name = "pinned_resource_2")
    private UUID pinnedResource2;

    @Column(name = "pinned_resource_3")
    private UUID pinnedResource3;
}