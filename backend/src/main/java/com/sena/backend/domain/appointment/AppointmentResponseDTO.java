package com.sena.backend.domain.appointment;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class AppointmentResponseDTO {
    private Long id;
    private Long consultationId;
    private Long patientId;
    private Long doctorId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
    private String cancellationReason;

    // Omitted fields: version, createdAt, updatedAt, notes (unless strictly required by the UI)
}