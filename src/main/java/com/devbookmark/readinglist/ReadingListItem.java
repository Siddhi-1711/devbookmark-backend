package com.devbookmark.readinglist;

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
        name = "reading_list",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reading_list_user_resource",
                columnNames = {"user_id", "resource_id"}
        ),
        indexes = {
                @Index(name = "idx_reading_list_user_id", columnList = "user_id"),
                @Index(name = "idx_reading_list_resource_id", columnList = "resource_id")
        }
)
public class ReadingListItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;
}