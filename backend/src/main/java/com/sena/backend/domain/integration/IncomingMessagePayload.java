package com.sena.backend.domain.integration;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class IncomingMessagePayload {
    private String messageId;
    private String phone;
    private String message;
    private OffsetDateTime timestamp;
}