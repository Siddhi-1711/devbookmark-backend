package com.devbookmark.publication;

import com.devbookmark.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "publications",
        indexes = {
                @Index(name = "idx_publications_owner_id", columnList = "owner_id"),
                @Index(name = "idx_publications_slug", columnList = "slug")
        }
)
public class Publication {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_publications_owner"))
    private User owner;

    // unique url slug e.g. "alice-dev"
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String bio;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}