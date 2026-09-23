package com.sena.backend.controller;

import com.sena.backend.domain.notification.NotificationCallbackRequestDTO;
import com.sena.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations/notifications")
@Tag(name = "Integration - Notifications", description = "Endpoints for receiving notification delivery statuses from external services")
public class WebhookController {

    private final NotificationService notificationService;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Receive notification status webhook", description = "Webhook to receive delivery status updates for sent notifications (e.g., from n8n or Evolution API). Secured by Integration API Key (X-API-Key).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook successfully processed and status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - API Key is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Notification record not found")
    })
    @PostMapping("/status")
    public ResponseEntity<Void> receiveStatus(@Valid @RequestBody NotificationCallbackRequestDTO dto) throws Exception {
        notificationService.updateNotificationStatus(dto);
        return ResponseEntity.ok().build();
    }
}