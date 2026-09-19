package com.sena.backend.domain.integration;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AppointmentConfirmationWebhookDTO {
    private Long appointmentId;
    private String patientPhone;
    private String patientName;
    private OffsetDateTime appointmentDate;
    private String doctorName; // Nuevo campo
    private String specialty;
}