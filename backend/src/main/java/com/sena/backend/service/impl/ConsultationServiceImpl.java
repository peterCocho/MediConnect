package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.consultation.ConsultationResponseDTO;
import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Patient;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final DoctorRepository doctorRepository;
    

    @Override
    @Transactional
    public ConsultationResponseDTO executeConsultation(Long consultationId, ExecuteConsultationRequestDTO request, Long doctorId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada con ID: " + consultationId));

        // Correct IDOR resolution: Find Doctor directly by doctorId
        Doctor requestingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de médico no encontrado con ID: " + doctorId));

        if (!consultation.getDoctor().getId().equals(requestingDoctor.getId())) {
            throw new AccessDeniedException("No tiene permisos para ejecutar una consulta que no le ha sido asignada");
        }

        // Validate that the consultation is in a scheduled state before execution
        if (consultation.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException("Solo las consultas en estado SCHEDULED pueden ser ejecutadas");
        }

        // Mutate clinical data
        consultation.setSystolicPressure(request.getSystolicPressure());
        consultation.setDiastolicPressure(request.getDiastolicPressure());
        consultation.setHeartRate(request.getHeartRate());
        consultation.setWeight(request.getWeight());
        consultation.setIcd10Code(request.getIcd10Code());
        consultation.setReasonForVisit(request.getReasonForVisit());
        consultation.setClinicalNotes(request.getClinicalNotes());
        consultation.setManagementPlan(request.getManagementPlan());

        // Seal consultation
        // Seal consultation and synchronize appointment state
        consultation.setStatus(AppointmentStatus.COMPLETED);

        if (consultation.getAppointment() != null) {
            consultation.getAppointment().setStatus(AppointmentStatus.COMPLETED);
        }

        return mapToDTO(consultationRepository.save(consultation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getPatientTimeline(Long medicalRecordId) {
        return consultationRepository.findByMedicalRecordIdAndStatusOrderByConsultationDateDesc(medicalRecordId, AppointmentStatus.COMPLETED)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO getConsultationByIdAndDoctorId(Long id, Long doctorId) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada con ID: " + id));

        if (!consultation.getDoctor().getId().equals(doctorId)) {
            throw new AccessDeniedException("Acceso denegado a esta consulta");
        }

        return mapToDTO(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationResponseDTO> getConsultationsByDoctorId(Long doctorId, Pageable pageable) {
        return consultationRepository.findByDoctorId(doctorId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationResponseDTO> getCompletedConsultationsByDoctorId(Long doctorId, Pageable pageable) {
        return consultationRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED, pageable)
                .map(this::mapToDTO);
    }

    private ConsultationResponseDTO mapToDTO(Consultation entity) {
        Long docId = (entity.getDoctor() != null) ? entity.getDoctor().getId() : null;
        Long recId = (entity.getMedicalRecord() != null) ? entity.getMedicalRecord().getId() : null;
        Long apptId = (entity.getAppointment() != null) ? entity.getAppointment().getId() : null;
        Patient patient = entity.getMedicalRecord() != null ? entity.getMedicalRecord().getPatient() : null;

        return ConsultationResponseDTO.builder()
                .id(entity.getId())
                .appointmentId(apptId)
                .consultationDate(entity.getConsultationDate())
                .status(entity.getStatus().name())
                .doctorId(docId)
                .medicalRecordId(recId)
                .patientId(patient != null ? patient.getId() : null)
                .identityDocument(patient != null ? patient.getIdentityDocument() : null)
                .fullName(patient != null ? patient.getFullName() : null)
                .phone(patient != null ? patient.getPhone() : null)
                .birthDate(patient != null ? patient.getBirthDate() : null)
                .systolicPressure(entity.getSystolicPressure())
                .diastolicPressure(entity.getDiastolicPressure())
                .heartRate(entity.getHeartRate())
                .weight(entity.getWeight())
                .icd10Code(entity.getIcd10Code())
                .reasonForVisit(entity.getReasonForVisit())
                .clinicalNotes(entity.getClinicalNotes())
                .managementPlan(entity.getManagementPlan())
                .build();
    }
}