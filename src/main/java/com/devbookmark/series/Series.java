package com.devbookmark.series;

import com.devbookmark.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "series",
        indexes = {
                @Index(name = "idx_series_owner_id", columnList = "owner_id"),
                @Index(name = "idx_series_slug", columnList = "slug")
        }
)
public class Series {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "cover_image", length = 2048)
    private String coverImage;

    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private boolean isComplete = false;

    @OneToMany(mappedBy = "series",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("partNumber asc")
    @Builder.Default
    private List<SeriesItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}