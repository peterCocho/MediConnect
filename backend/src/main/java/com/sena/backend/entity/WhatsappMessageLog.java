package com.sena.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "whatsapp_message_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsappMessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The phone number of the patient sending the message
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    // The actual text content received from WhatsApp
    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    // Flag to determine if the receptionist has read the message in the UI
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) {
            receivedAt = OffsetDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}