package com.sena.backend.domain.consultation;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class ConsultationResponseDTO {
    private Long id;
    private Long appointmentId;
    private OffsetDateTime consultationDate;
    private String status;
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private Integer heartRate;
    private BigDecimal weight;
    private String icd10Code;
    private String reasonForVisit;
    private String clinicalNotes;
    private String managementPlan;

    // Crucial for the frontend to render meaningful UI instead of raw IDs
    private Long doctorId;
    private Long medicalRecordId;
}