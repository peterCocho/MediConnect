package com.sena.backend.controller;

import com.sena.backend.domain.integration.WhatsappMessageLogDTO;
import com.sena.backend.service.WhatsappLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/whatsapp/messages")
@RequiredArgsConstructor
public class WhatsappLogController {

    private final WhatsappLogService whatsappLogService;

    // Endpoint for the receptionist dashboard to poll/fetch unread messages
    @GetMapping("/unread")
    public ResponseEntity<List<WhatsappMessageLogDTO>> getUnreadMessages() {
        return ResponseEntity.ok(whatsappLogService.getUnreadMessages());
    }

    // Endpoint for the receptionist to dismiss a message after processing it
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        whatsappLogService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}