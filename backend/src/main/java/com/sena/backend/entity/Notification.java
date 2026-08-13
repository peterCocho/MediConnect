package com.sena.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destination_number", nullable = false, length = 20)
    private String destinationNumber;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "consultation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_consultation"))
    private Consultation consultation;

    // Añade esto debajo de providerId
    @Column(name = "notification_type", nullable = false, length = 50)
    private String type;

}
