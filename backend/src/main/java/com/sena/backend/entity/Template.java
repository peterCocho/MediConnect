package com.sena.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "clinical_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String content;

    @Column(columnDefinition = "uuid")
    private UUID createdBy;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
    }
}
