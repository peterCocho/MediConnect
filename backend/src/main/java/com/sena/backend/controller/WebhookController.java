package com.sena.backend.controller;

import com.sena.backend.domain.notification.NotificationCallbackRequestDTO;
import com.sena.backend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// Cambiamos la ruta para que caiga bajo el filtro de seguridad de integraciones
@RequestMapping("/api/integrations/notifications")
public class WebhookController {

    private final NotificationService notificationService;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/status")
    public ResponseEntity<Void> receiveStatus(@Valid @RequestBody NotificationCallbackRequestDTO dto) throws Exception {
        // La validación de X-API-Key ya fue manejada por IntegrationApiKeyFilter.
        // Si la petición llega aquí, está autorizada.
        notificationService.updateNotificationStatus(dto);
        return ResponseEntity.ok().build();
    }
}