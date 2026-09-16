package com.sena.backend.domain.integration;

import lombok.Data;

@Data
public class N8nErrorWebhookDTO {
    private String patientName;
    private String patientPhone;
    private String module;
    private String rawErrorMessage;
}