package com.devbookmark.resource;

import com.devbookmark.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "resources",
        indexes = {
                @Index(name = "idx_resources_created_at", columnList = "created_at"),
                @Index(name = "idx_resources_owner_id", columnList = "owner_id"),
                @Index(name = "idx_resources_type", columnList = "type")
        }
)
public class Resource {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_resources_owner"))
    private User owner;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 2048)
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // mapped join entities
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ResourceTag> resourceTags = new HashSet<>();

    // ✅ ADD HERE (bottom of class)
    @PrePersist
    @PreUpdate
    public void normalize() {
        if (title != null) title = title.trim();
        if (description != null) description = description.trim();
        if (link != null) link = link.trim();
    }

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResourceLike> likes = new HashSet<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResourceSave> saves = new HashSet<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ResourceVisibility visibility = ResourceVisibility.PUBLIC;

    @Column(columnDefinition = "TEXT")
    private String content;  // full article content (markdown/html)

    @Column(name = "cover_image", length = 2048)
    private String coverImage;  // cover image URL

    @Column(name = "file_url", length = 2048)
    private String fileUrl;  // uploaded file URL (PDF, DOC, TXT, image)
    @Column(name = "file_name", length = 255)
    private String fileName;   // original filename (e.g., resume.docx)

    @Column(name = "file_content_type", length = 100)
    private String fileContentType; // e.g., application/vnd.openxmlformats-officedocument.wordprocessingml.document
    @Column(name = "read_time_minutes")
    private Integer readTimeMinutes;  // estimated read time

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = false;  // draft vs live

    @Column(name = "published_at")
    private Instant publishedAt;  // when it was published
}