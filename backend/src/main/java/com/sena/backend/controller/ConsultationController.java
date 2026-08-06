package com.sena.backend.controller;

import com.sena.backend.domain.consultation.ConsultationResponseDTO;
import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.entity.Consultation;
import com.sena.backend.security.CustomUserDetails;
import com.sena.backend.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
// Protect the entire controller at the class level based on your rule
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
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long authenticatedDoctorId = userDetails.getDoctorId();

        if (authenticatedDoctorId == null) {
            throw new IllegalStateException("The authenticated user does not have a valid Doctor ID linked.");
        }

        Consultation consultation = consultationService.executeConsultation(id, request, authenticatedDoctorId);
        return ResponseEntity.ok(convertToResponseDTO(consultation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> getConsultationById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long authenticatedDoctorId = userDetails.getDoctorId();

        Consultation consultation = consultationService.getConsultationByIdAndDoctorId(id, authenticatedDoctorId);
        return ResponseEntity.ok(convertToResponseDTO(consultation));
    }

    @GetMapping
    public ResponseEntity<Page<ConsultationResponseDTO>> getConsultationsForAuthenticatedDoctor(
            Authentication authentication,
            Pageable pageable
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long authenticatedDoctorId = userDetails.getDoctorId();

        if (authenticatedDoctorId == null) {
            throw new IllegalStateException("The authenticated user does not have a valid Doctor ID linked.");
        }

        Page<Consultation> consultations = consultationService.getConsultationsByDoctorId(authenticatedDoctorId, pageable);
        return ResponseEntity.ok(consultations.map(this::convertToResponseDTO));
    }

    private ConsultationResponseDTO convertToResponseDTO(Consultation consultation) {
        return ConsultationResponseDTO.builder()
                .id(consultation.getId())
                .appointmentId(consultation.getAppointment() != null ? consultation.getAppointment().getId() : null)
                .consultationDate(consultation.getConsultationDate())
                .status(consultation.getStatus())
                .systolicPressure(consultation.getSystolicPressure())
                .diastolicPressure(consultation.getDiastolicPressure())
                .heartRate(consultation.getHeartRate())
                .weight(consultation.getWeight())
                .icd10Code(consultation.getIcd10Code())
                .reasonForVisit(consultation.getReasonForVisit())
                .clinicalNotes(consultation.getClinicalNotes())
                .managementPlan(consultation.getManagementPlan())
                .doctorId(consultation.getDoctor() != null ? consultation.getDoctor().getId() : null)
                .medicalRecordId(consultation.getMedicalRecord() != null ? consultation.getMedicalRecord().getId() : null)
                .build();
    }
}