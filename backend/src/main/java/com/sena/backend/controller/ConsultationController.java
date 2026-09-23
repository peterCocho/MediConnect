package com.sena.backend.controller;

import com.sena.backend.domain.consultation.ConsultationResponseDTO;
import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.security.CustomUserDetails;
import com.sena.backend.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Consultations", description = "Endpoints for managing medical consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }
    
    @Operation(summary = "Execute a consultation", description = "Executes and finalizes a medical consultation with notes, diagnosis, and treatment plan. Requires DOCTOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultation successfully executed"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or consultation cannot be executed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
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

    @Operation(summary = "Get consultation by ID", description = "Retrieves the details of a specific consultation for the authenticated doctor. Requires DOCTOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultation successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Consultation not found or does not belong to the doctor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> getConsultationById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long authenticatedDoctorId = extractDoctorId(authentication);
        ConsultationResponseDTO response = consultationService.getConsultationByIdAndDoctorId(id, authenticatedDoctorId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get doctor's consultations", description = "Retrieves a paginated list of consultations assigned to the authenticated doctor. Requires DOCTOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of consultations successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping
    public ResponseEntity<Page<ConsultationResponseDTO>> getConsultationsForAuthenticatedDoctor(
            Authentication authentication,
            Pageable pageable
    ) {
        Long authenticatedDoctorId = extractDoctorId(authentication);
        Page<ConsultationResponseDTO> consultations = consultationService.getConsultationsByDoctorId(authenticatedDoctorId, pageable);
        return ResponseEntity.ok(consultations);
    }

    @Operation(summary = "Get completed patients", description = "Retrieves a paginated list of completed consultations for the authenticated doctor. Requires DOCTOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of completed consultations successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping("/patients")
    public ResponseEntity<Page<ConsultationResponseDTO>> getCompletedPatientsForAuthenticatedDoctor(
            Authentication authentication,
            Pageable pageable
    ) {
        Long authenticatedDoctorId = extractDoctorId(authentication);
        return ResponseEntity.ok(consultationService.getCompletedConsultationsByDoctorId(authenticatedDoctorId, pageable));
    }

    // HU-07: Immutable clinical timeline endpoint
    @Operation(summary = "Get patient clinical timeline", description = "Retrieves the immutable clinical timeline of consultations for a specific medical record. Requires DOCTOR role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient clinical timeline successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Medical record not found")
    })
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
            // El mensaje lanzado hacia el cliente debe estar en español
            throw new IllegalStateException("El usuario autenticado no tiene un ID de médico válido vinculado.");
        }

        return doctorId;
    }
}