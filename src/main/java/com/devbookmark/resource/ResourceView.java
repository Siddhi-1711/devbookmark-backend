package com.devbookmark.resource;

import com.devbookmark.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "resource_views",
        indexes = {
                @Index(name = "idx_resource_views_resource_id", columnList = "resource_id"),
                @Index(name = "idx_resource_views_user_id", columnList = "user_id"),
                @Index(name = "idx_resource_views_created_at", columnList = "created_at")
        }
)
public class ResourceView {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    // nullable - anonymous users can view too
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // for anonymous users
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "read_completed")
    private boolean readCompleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}