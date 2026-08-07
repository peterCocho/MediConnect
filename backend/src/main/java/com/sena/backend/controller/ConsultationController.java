package com.sena.backend.controller;

import com.sena.backend.domain.consultation.ConsultationResponseDTO;
import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.security.CustomUserDetails;
import com.sena.backend.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@PreAuthorize("hasRole('DOCTOR')")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<ConsultationResponseDTO> executeConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ExecuteConsultationRequestDTO request,
            Authentication authentication
    ) {
        Long authenticatedDoctorId = extractDoctorId(authentication);
        ConsultationResponseDTO response = consultationService.executeConsultation(id, request, authenticatedDoctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> getConsultationById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long authenticatedDoctorId = extractDoctorId(authentication);
        ConsultationResponseDTO response = consultationService.getConsultationByIdAndDoctorId(id, authenticatedDoctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ConsultationResponseDTO>> getConsultationsForAuthenticatedDoctor(
            Authentication authentication,
            Pageable pageable
    ) {
        Long authenticatedDoctorId = extractDoctorId(authentication);
        Page<ConsultationResponseDTO> consultations = consultationService.getConsultationsByDoctorId(authenticatedDoctorId, pageable);
        return ResponseEntity.ok(consultations);
    }

    // HU-07: Immutable clinical timeline endpoint
    @GetMapping("/patient-timeline/{medicalRecordId}")
    public ResponseEntity<List<ConsultationResponseDTO>> getPatientTimeline(
            @PathVariable Long medicalRecordId
    ) {
        List<ConsultationResponseDTO> timeline = consultationService.getPatientTimeline(medicalRecordId);
        return ResponseEntity.ok(timeline);
    }

    private Long extractDoctorId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long doctorId = userDetails.getDoctorId();

        if (doctorId == null) {
            throw new IllegalStateException("The authenticated user does not have a valid Doctor ID linked.");
        }

        return doctorId;
    }
}