package com.sena.backend.controller;

import com.sena.backend.domain.notification.NotificationCallbackRequestDTO;
import com.sena.backend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/integrations/notifications")
public class WebhookController {

    private final NotificationService notificationService;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/status")
    public ResponseEntity<Void> receiveStatus(@Valid @RequestBody NotificationCallbackRequestDTO dto) throws Exception {
        notificationService.updateNotificationStatus(dto);
        return ResponseEntity.ok().build();
    }
}