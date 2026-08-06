package com.sena.backend.domain.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BookAppointmentRequestDTO {
    @NotNull(message = "Patient ID is mandatory")
    private Long patientId;

    @NotNull(message = "Doctor ID is mandatory")
    private Long doctorId;

    @NotNull(message = "Start time is mandatory")
    @FutureOrPresent(message = "Start time cannot be in the past")
    private OffsetDateTime startTime;

    @NotNull(message = "End time is mandatory")
    @Future(message = "End time must be in the future")
    private OffsetDateTime endTime;
}