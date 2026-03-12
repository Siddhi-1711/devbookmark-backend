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
        name = "resource_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resource_likes_user_resource",
                columnNames = {"user_id","resource_id"}
        ),
        indexes = {
                @Index(name = "idx_resource_likes_resource_id", columnList = "resource_id"),
                @Index(name = "idx_resource_likes_user_id", columnList = "user_id")
        }
)
public class ResourceLike {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="resource_id", nullable=false)
    private Resource resource;

    @CreationTimestamp
    private Instant createdAt;
}