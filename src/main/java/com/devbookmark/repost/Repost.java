package com.devbookmark.repost;

import com.devbookmark.resource.Resource;
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
        name = "reposts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reposts_user_resource",
                columnNames = {"user_id", "resource_id"}
        ),
        indexes = {
                @Index(name = "idx_reposts_user_id", columnList = "user_id"),
                @Index(name = "idx_reposts_resource_id", columnList = "resource_id"),
                @Index(name = "idx_reposts_created_at", columnList = "created_at")
        }
)
public class Repost {

    @Id
    @GeneratedValue
    private UUID id;

    // who reposted
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // what was reposted
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    // optional comment on repost
    @Column(length = 300)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}