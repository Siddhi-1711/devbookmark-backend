package com.devbookmark.collection;

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
        name = "collection_resources",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_collection_resources_collection_resource",
                columnNames = {"collection_id", "resource_id"}
        ),
        indexes = {
                @Index(name = "idx_collection_resources_collection_id", columnList = "collection_id"),
                @Index(name = "idx_collection_resources_resource_id", columnList = "resource_id")
        }
)
public class CollectionResource {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_collection_resources_collection"))
    private Collection collection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_collection_resources_resource"))
    private Resource resource;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;
}