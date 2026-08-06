package com.sena.backend.service;

import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConsultationService {
    Consultation executeConsultation(Long id, ExecuteConsultationRequestDTO request, Long authenticatedDoctorId);
    Consultation getConsultationByIdAndDoctorId(Long id, Long doctorId);
    Page<Consultation> getConsultationsByDoctorId(Long doctorId, Pageable pageable);
}