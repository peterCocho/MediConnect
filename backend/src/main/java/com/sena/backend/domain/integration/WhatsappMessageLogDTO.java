package com.sena.backend.domain.integration;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class WhatsappMessageLogDTO {
    private Long id;
    private String phoneNumber;
    private String messageBody;
    private OffsetDateTime receivedAt;
    private String patientName;
    private String specialty;
}