package com.sena.backend.controller;

import com.sena.backend.domain.appointment.CancelAppointmentRequestDTO;
import com.sena.backend.domain.integration.IncomingMessagePayload;
import com.sena.backend.entity.WhatsappMessageLog;
import com.sena.backend.repository.WhatsappMessageLogRepository;
import com.sena.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
@Tag(name = "Integrations", description = "Endpoints for external integrations, such as n8n and WhatsApp webhooks")
public class IntegrationController {

    private final WhatsappMessageLogRepository messageLogRepository;
    private final AppointmentService appointmentService;

    // Webhook for n8n to deliver incoming WhatsApp messages
    @Operation(summary = "Receive incoming WhatsApp message", description = "Webhook for n8n to deliver incoming WhatsApp messages. Requires Integration API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully received and logged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - API Key is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/whatsapp/messages")
    public ResponseEntity<Void> receiveIncomingMessage(@RequestBody IncomingMessagePayload payload) {
        WhatsappMessageLog log = WhatsappMessageLog.builder()
                .phoneNumber(payload.getPhone())
                .messageBody(payload.getMessage())
                .build();

        messageLogRepository.save(log);
        return ResponseEntity.ok().build();
    }

    // Endpoint explicitly for n8n to confirm an appointment via API Key
    @Operation(summary = "Confirm appointment from integration", description = "Confirms an appointment. Explicitly designed for n8n via API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully confirmed"),
            @ApiResponse(responseCode = "400", description = "Appointment cannot be confirmed in its current state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - API Key is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PatchMapping("/appointments/{id}/confirm")
    public ResponseEntity<Void> confirmAppointmentFromIntegration(@PathVariable Long id) {
        // Calls the exact same business logic as the frontend controller
        appointmentService.confirmAppointment(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint explicitly for n8n to cancel an appointment via API Key,
    // triggered by the "Cancelar cita" button in the WhatsApp reminder template.
    // Reuses the same CancelAppointmentRequestDTO as the receptionist-facing endpoint,
    // whose "reason" field is @NotBlank -> n8n must always send a non-empty reason.
    @Operation(summary = "Cancel appointment from integration", description = "Cancels an appointment. Triggered by WhatsApp reminder templates via n8n. Explicitly designed for n8n via API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully cancelled"),
            @ApiResponse(responseCode = "400", description = "Appointment cannot be cancelled in its current state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - API Key is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PatchMapping("/appointments/{id}/cancel")
    public ResponseEntity<Void> cancelAppointmentFromIntegration(@PathVariable Long id,
                                                                 @RequestBody(required = false) CancelAppointmentRequestDTO body) {
        String reason = (body != null && body.reason() != null && !body.reason().isBlank())
                ? body.reason()
                : "Cancelado por el paciente vía WhatsApp";

        appointmentService.cancelAppointment(id, reason);
        return ResponseEntity.ok().build();
    }
}