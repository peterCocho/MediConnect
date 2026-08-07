package com.sena.backend.service;

import com.sena.backend.domain.consultation.ConsultationResponseDTO;
import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConsultationService {

    // Core clinical execution (HU-06) - Cambiado a Long para alinearlo con el controlador
    ConsultationResponseDTO executeConsultation(Long id, ExecuteConsultationRequestDTO request, Long doctorId);

    // Immutable clinical timeline (HU-07)
    List<ConsultationResponseDTO> getPatientTimeline(Long medicalRecordId);

    // Existing queries updated to return DTOs instead of Entities
    ConsultationResponseDTO getConsultationByIdAndDoctorId(Long id, Long doctorId);

    Page<ConsultationResponseDTO> getConsultationsByDoctorId(Long doctorId, Pageable pageable);
}