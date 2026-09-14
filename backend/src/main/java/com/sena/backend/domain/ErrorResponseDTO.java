package com.sena.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private String message;
    private OffsetDateTime timestamp;
    private int status;
}