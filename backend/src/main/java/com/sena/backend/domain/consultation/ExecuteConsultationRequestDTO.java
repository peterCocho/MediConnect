package com.sena.backend.domain.consultation;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExecuteConsultationRequestDTO {

    @NotNull(message = "La presión sistólica es obligatoria")
    @Min(value = 1, message = "La presión sistólica debe ser mayor a 0")
    @Max(value = 299, message = "La presión sistólica excede el límite permitido")
    private Integer systolicPressure;

    @NotNull(message = "La presión diastólica es obligatoria")
    @Min(value = 1, message = "La presión diastólica debe ser mayor a 0")
    @Max(value = 199, message = "La presión diastólica excede el límite permitido")
    private Integer diastolicPressure;

    @NotNull(message = "La frecuencia cardíaca es obligatoria")
    @Min(value = 1, message = "La frecuencia cardíaca debe ser mayor a 0")
    @Max(value = 299, message = "La frecuencia cardíaca excede el límite permitido")
    private Integer heartRate;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
    private BigDecimal weight;

    @Size(max = 10)
    @NotBlank(message = "El código CIE-10 es obligatorio")
    private String icd10Code;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    private String reasonForVisit;

    private String clinicalNotes;

    @NotBlank(message = "El plan de manejo es obligatorio")
    private String managementPlan;
}