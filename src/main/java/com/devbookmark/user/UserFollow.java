package com.devbookmark.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "user_follows",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_follows_follower_followee",
                columnNames = {"follower_id", "followee_id"}
        ),
        indexes = {
                @Index(name = "idx_user_follows_follower_id", columnList = "follower_id"),
                @Index(name = "idx_user_follows_followee_id", columnList = "followee_id")
        }
)
public class UserFollow {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followee_id", nullable = false)
    private User followee;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}