package com.sena.backend.domain.appointment;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequestDTO(
        @NotBlank(message = "La razón de cancelación es obligatoria")
        String reason
) {}