package com.devbookmark.resource;

import com.devbookmark.tag.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "resource_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resource_tags_resource_tag",
                columnNames = {"resource_id", "tag_id"}
        ),
        indexes = {
                @Index(name = "idx_resource_tags_resource_id", columnList = "resource_id"),
                @Index(name = "idx_resource_tags_tag_id", columnList = "tag_id")
        }
)
public class ResourceTag {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_resource_tags_resource"))
    private Resource resource;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_resource_tags_tag"))
    private Tag tag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}