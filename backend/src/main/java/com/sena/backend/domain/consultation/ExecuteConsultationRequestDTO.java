package com.sena.backend.domain.consultation;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExecuteConsultationRequestDTO {

    // Added mandatory appointment link.
    // A consultation cannot be executed in a vacuum; it requires a scheduled appointment.
    @NotNull(message = "Appointment ID is mandatory")
    private Long appointmentId;

    @NotNull
    @Min(1)
    @Max(299)
    @Positive(message = "Systolic pressure must be positive")
    private Integer systolicPressure;

    @NotNull
    @Min(1)
    @Max(199)
    @Positive(message = "Diastolic pressure must be positive")
    private Integer diastolicPressure;

    @NotNull
    @Min(1)
    @Max(299)
    @Positive(message = "Heart rate must be positive")
    private Integer heartRate;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal weight;

    @Size(max = 10)
    @NotBlank(message = "ICD-10 code is mandatory")
    private String icd10Code;

    @NotBlank(message = "Reason for visit is mandatory")
    private String reasonForVisit;

    private String clinicalNotes;

    @NotBlank(message = "Management plan is mandatory")
    private String managementPlan;
}