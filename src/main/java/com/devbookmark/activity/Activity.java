package com.devbookmark.activity;

import com.devbookmark.user.User;
import com.devbookmark.resource.Resource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "activities",
        indexes = {
                @Index(name = "idx_activities_user_id", columnList = "user_id"),
                @Index(name = "idx_activities_created_at", columnList = "created_at"),
                @Index(name = "idx_activities_user_created", columnList = "user_id, created_at")
        }
)
public class Activity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


}