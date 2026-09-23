package com.sena.backend.controller;

import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Read-only lookup used by n8n to check if an incoming WhatsApp number
// already belongs to a registered patient, before offering to book an appointment.
// Secured by IntegrationApiKeyFilter (X-API-Key), same as the rest of /api/integrations/**.
// Patient CREATION stays exclusive to receptionists via PatientController (JWT + ROLE_RECEPTION).
@RestController
@RequestMapping("/api/integrations/patients")
@RequiredArgsConstructor
@Tag(name = "Integration - Patients", description = "Endpoints for external integrations to query patient data")
public class IntegrationPatientController {

    private final PatientService patientService;

    @Operation(summary = "Lookup patient by phone", description = "Checks if an incoming WhatsApp number belongs to a registered patient. Explicitly designed for n8n via Integration API Key (X-API-Key).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient successfully found"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid phone parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - API Key is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Patient not found with the provided phone number")
    })
    @GetMapping("/lookup")
    public ResponseEntity<PatientResponse> lookupByPhone(@RequestParam String phone) {
        // Throws ResourceNotFoundException -> GlobalExceptionHandler returns a clean 404 JSON
        // n8n's "If" node can branch on the HTTP status code of this call.
        return ResponseEntity.ok(patientService.getPatientByPhone(phone));
    }
}