package com.sena.backend.controller;

import com.sena.backend.domain.integration.N8nErrorWebhookDTO;
import com.sena.backend.entity.ErrorLog;
import com.sena.backend.repository.ErrorLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Integration - n8n Errors", description = "Endpoints for receiving error webhooks from n8n automation flows")
public class N8nIntegrationController {

    private final ErrorLogRepository errorLogRepository;

    @Operation(summary = "Receive n8n error webhook", description = "Receives error details from n8n workflows, translates them into human-readable messages, and logs them as system errors. Secured by Integration API Key (X-API-Key).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Error webhook successfully received and logged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - API Key is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/webhook-n8n")
    public ResponseEntity<Void> receiveN8nError(@RequestBody N8nErrorWebhookDTO dto) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setTimestamp(OffsetDateTime.now());
        errorLog.setStatusCode(500);
        errorLog.setExceptionType("Fallo de Integración (n8n)");
        errorLog.setPath(dto.getModule() != null ? dto.getModule() : "Módulo Desconocido");

        String patientName = dto.getPatientName() != null ? dto.getPatientName() : "Paciente";
        String patientPhone = dto.getPatientPhone() != null ? dto.getPatientPhone() : "Sin teléfono";

        // Translate the raw technical error into a clean, human-readable message for the receptionist
        String friendlyError = translateTechnicalError(dto.getRawErrorMessage());

        String finalMessage = String.format("No se pudo enviar el mensaje a %s (%s). Motivo: %s",
                patientName, patientPhone, friendlyError);

        errorLog.setMessage(finalMessage);
        errorLogRepository.save(errorLog);

        return ResponseEntity.ok().build();
    }

    // Helper method to map technical strings to clear user-friendly messages
    private String translateTechnicalError(String rawError) {
        if (rawError == null || rawError.isBlank()) {
            return "Error desconocido en el servicio de mensajería.";
        }

        // Map common technical errors to readable Spanish messages
        if (rawError.contains("Connection Closed")) {
            return "La conexión con el servicio de WhatsApp se cerró inesperadamente.";
        } else if (rawError.contains("Internal Server Error") || rawError.contains("500")) {
            return "El servidor externo de mensajería no está respondiendo (Error 500).";
        } else if (rawError.contains("ETIMEDOUT") || rawError.contains("Timeout")) {
            return "Tiempo de espera agotado al conectar con WhatsApp.";
        } else if (rawError.contains("Unauthorized") || rawError.contains("401")) {
            return "Fallo de autenticación con la pasarela de mensajes.";
        } else if (rawError.contains("ENOTFOUND") || rawError.contains("ECONNREFUSED")) {
            return "No se pudo establecer comunicación con el servidor de red.";
        }

        // Default fallback keeping it clean if it doesn't match specific patterns
        return "Fallo en el procesamiento de la pasarela de WhatsApp.";
    }
}