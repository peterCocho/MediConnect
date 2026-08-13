package com.sena.backend.controller;

import com.sena.backend.domain.integration.IncomingMessagePayload;
import com.sena.backend.entity.WhatsappMessageLog;
import com.sena.backend.repository.WhatsappMessageLogRepository;
import com.sena.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final WhatsappMessageLogRepository messageLogRepository;
    private final AppointmentService appointmentService;

    // Webhook for n8n to deliver incoming WhatsApp messages
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
    @PatchMapping("/appointments/{id}/confirm")
    public ResponseEntity<Void> confirmAppointmentFromIntegration(@PathVariable Long id) {
        // Calls the exact same business logic as the frontend controller
        appointmentService.confirmAppointment(id);
        return ResponseEntity.ok().build();
    }
}