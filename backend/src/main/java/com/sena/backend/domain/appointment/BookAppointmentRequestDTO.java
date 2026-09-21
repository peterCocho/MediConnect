package com.sena.backend.domain.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startTime;

    @NotNull(message = "La fecha y hora de fin son obligatorias")
    @Future(message = "La fecha y hora de fin deben ser en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endTime;
}