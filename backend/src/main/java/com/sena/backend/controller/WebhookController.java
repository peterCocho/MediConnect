package com.sena.backend.controller;

import com.sena.backend.domain.NotificationCallbackRequestDTO;
import com.sena.backend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/n8n")
public class WebhookController {

    private final NotificationService notificationService;

    @Value("${n8n.webhook.api-key}")
    private String configuredApiKey;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/status")
    public ResponseEntity<Void> receiveStatus(@RequestHeader(value = "x-api-key", required = false) String apiKey,
                                              @Valid @RequestBody NotificationCallbackRequestDTO dto) throws Exception {
        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notificationService.updateNotificationStatus(dto);
        return ResponseEntity.ok().build();
    }
}
