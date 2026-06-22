package com.sena.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private String mensaje;
    private OffsetDateTime fecha;
    private int status;
}
