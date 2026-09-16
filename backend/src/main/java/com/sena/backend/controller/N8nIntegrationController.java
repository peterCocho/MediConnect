package com.sena.backend.controller;

import com.sena.backend.domain.integration.N8nErrorWebhookDTO;
import com.sena.backend.entity.ErrorLog;
import com.sena.backend.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/integrations/errors")
@RequiredArgsConstructor
public class N8nIntegrationController {

    private final ErrorLogRepository errorLogRepository;

    // Endpoint secured by IntegrationApiKeyFilter, ignores JWT
    @PostMapping("/webhook-n8n")
    public ResponseEntity<Void> receiveN8nError(@RequestBody N8nErrorWebhookDTO dto) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setTimestamp(OffsetDateTime.now());
        errorLog.setExceptionType("Fallo de Integración (n8n)");
        errorLog.setPath(dto.getModule());
        errorLog.setMessage("No se pudo enviar el mensaje a " + dto.getPatientName() +
                " (" + dto.getPatientPhone() + "). Error de n8n: " + dto.getRawErrorMessage());

        errorLogRepository.save(errorLog);
        return ResponseEntity.ok().build();
    }
}