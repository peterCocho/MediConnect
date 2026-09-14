package com.sena.backend.domain.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class BookAppointmentRequestDTO {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long patientId;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long doctorId;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @FutureOrPresent(message = "La fecha y hora de inicio no pueden estar en el pasado")
    private OffsetDateTime startTime;

    @NotNull(message = "La fecha y hora de fin son obligatorias")
    @Future(message = "La fecha y hora de fin deben ser en el futuro")
    private OffsetDateTime endTime;
}