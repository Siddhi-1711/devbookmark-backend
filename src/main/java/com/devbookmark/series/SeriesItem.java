package com.devbookmark.series;

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
        name = "series_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_series_items_series_part",
                        columnNames = {"series_id", "part_number"}
                ),
                @UniqueConstraint(
                        name = "uk_series_items_series_resource",
                        columnNames = {"series_id", "resource_id"}
                ),
                @UniqueConstraint(
                        name = "uk_series_items_resource_global",
                        columnNames = {"resource_id"}
                )
        },
        indexes = {
                @Index(name = "idx_series_items_series_id", columnList = "series_id"),
                @Index(name = "idx_series_items_resource_id", columnList = "resource_id")
        }
)
public class SeriesItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "part_number", nullable = false)
    private int partNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}