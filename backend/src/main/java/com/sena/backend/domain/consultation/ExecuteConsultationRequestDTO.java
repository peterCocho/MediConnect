package com.sena.backend.domain.consultation;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExecuteConsultationRequestDTO {

    @NotNull
    @Min(1)
    @Max(299)
    private Integer systolicPressure;

    @NotNull
    @Min(1)
    @Max(199)
    private Integer diastolicPressure;

    @NotNull
    @Min(1)
    @Max(299)
    private Integer heartRate;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal weight;

    @Size(max = 10)
    private String icd10Code;

    private String reasonForVisit;

    private String clinicalNotes;

    private String managementPlan;
}
