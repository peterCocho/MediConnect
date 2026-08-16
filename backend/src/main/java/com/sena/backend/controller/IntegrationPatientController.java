package com.sena.backend.controller;

import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.service.PatientService;
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
public class IntegrationPatientController {

    private final PatientService patientService;

    @GetMapping("/lookup")
    public ResponseEntity<PatientResponse> lookupByPhone(@RequestParam String phone) {
        // Throws ResourceNotFoundException -> GlobalExceptionHandler returns a clean 404 JSON
        // n8n's "If" node can branch on the HTTP status code of this call.
        return ResponseEntity.ok(patientService.getPatientByPhone(phone));
    }
}