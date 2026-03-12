package com.devbookmark.tag;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_tags_name", columnNames = "name"),
        indexes = @Index(name = "idx_tags_name", columnList = "name")
)
public class Tag {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name; // stored as lowercase
}