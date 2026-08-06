package com.sena.backend.service.impl;

import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Appointment;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.service.ConsultationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    @Override
    @Transactional
    public Consultation executeConsultation(Long id, ExecuteConsultationRequestDTO dto, Long authenticatedDoctorId) {

        // Validation of existence and ownership happens together
        Consultation consultation = getConsultationByIdAndDoctorId(id, authenticatedDoctorId);

        if (!"SCHEDULED".equals(consultation.getStatus())) {
            throw new BusinessRuleException("Only scheduled consultations can be executed.");
        }

        if (OffsetDateTime.now().isBefore(consultation.getConsultationDate())) {
            throw new BusinessRuleException("Cannot execute a consultation before its scheduled time.");
        }

        if (dto.getDiastolicPressure() >= dto.getSystolicPressure()) {
            throw new BusinessRuleException("Diastolic pressure cannot be greater than or equal to systolic pressure.");
        }

        // Update clinical fields
        consultation.setSystolicPressure(dto.getSystolicPressure());
        consultation.setDiastolicPressure(dto.getDiastolicPressure());
        consultation.setHeartRate(dto.getHeartRate());
        consultation.setWeight(dto.getWeight());
        consultation.setIcd10Code(dto.getIcd10Code());
        consultation.setReasonForVisit(dto.getReasonForVisit());
        consultation.setClinicalNotes(dto.getClinicalNotes());
        consultation.setManagementPlan(dto.getManagementPlan());

        // Update Consultation status
        consultation.setStatus("COMPLETED");

        // Sync the linked Appointment status within the same transaction to prevent data anomalies
        Appointment appointment = consultation.getAppointment();
        if (appointment != null) {
            appointment.setStatus("COMPLETED");
        }

        return consultationRepository.save(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public Consultation getConsultationByIdAndDoctorId(Long id, Long doctorId) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));

        // Hard enforcement of clinical ownership
        if (!consultation.getDoctor().getId().equals(doctorId)) {
            throw new BusinessRuleException("Unauthorized to access a consultation assigned to another doctor.");
        }

        return consultation;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Consultation> getConsultationsByDoctorId(Long doctorId, Pageable pageable) {
        return consultationRepository.findByDoctorId(doctorId, pageable);
    }
}