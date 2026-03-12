package com.devbookmark.tag;

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
        name = "tag_follows",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tag_follows_user_tag",
                columnNames = {"user_id", "tag_id"}
        ),
        indexes = {
                @Index(name = "idx_tag_follows_user_id", columnList = "user_id"),
                @Index(name = "idx_tag_follows_tag_id", columnList = "tag_id")
        }
)
public class TagFollow {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}