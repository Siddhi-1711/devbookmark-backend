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
        name = "resource_saves",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resource_saves_user_resource",
                columnNames = {"user_id","resource_id"}
        ),
        indexes = {
                @Index(name = "idx_resource_saves_resource_id", columnList = "resource_id"),
                @Index(name = "idx_resource_saves_user_id", columnList = "user_id")
        }
)
public class ResourceSave {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @CreationTimestamp
    private Instant createdAt;
}