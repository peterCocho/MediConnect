package com.sena.backend.controller;

import com.sena.backend.domain.integration.WhatsappMessageLogDTO;
import com.sena.backend.service.WhatsappLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/whatsapp/messages")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Messages", description = "Endpoints for managing incoming WhatsApp messages from patients")
public class WhatsappLogController {

    private final WhatsappLogService whatsappLogService;

    // Endpoint for the receptionist dashboard to poll/fetch unread messages
    @Operation(summary = "Get unread messages", description = "Retrieves a list of all unread incoming WhatsApp messages for the dashboard. Accessible by RECEPTIONIST role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of unread messages successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping("/unread")
    public ResponseEntity<List<WhatsappMessageLogDTO>> getUnreadMessages() {
        return ResponseEntity.ok(whatsappLogService.getUnreadMessages());
    }

    // Endpoint for the receptionist to dismiss a message after processing it
    @Operation(summary = "Mark message as read", description = "Marks a specific WhatsApp message as read/processed so it no longer appears in the unread queue. Accessible by RECEPTIONIST role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Message log not found")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        whatsappLogService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}