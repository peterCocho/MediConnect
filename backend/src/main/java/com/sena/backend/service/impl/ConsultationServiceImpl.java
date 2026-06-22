package com.sena.backend.service.impl;

import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.domain.consultation.ScheduleConsultationRequestDTO;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.MedicalRecordRepository;
import com.sena.backend.service.ConsultationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ConsultationServiceImpl implements ConsultationService {

    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConsultationServiceImpl(DoctorRepository doctorRepository,
                                   MedicalRecordRepository medicalRecordRepository,
                                   ConsultationRepository consultationRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.consultationRepository = consultationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Schedule consultation following strict business rules:
     * 1) Resolve MedicalRecord by patient_id; if missing throw ResourceNotFoundException
     * 2) Use @Transactional and lock doctor record via findByIdForUpdate to avoid phantom inserts
     * 3) Check overlapping consultations in [requested, requested + 30 minutes);
     *    if any, throw BusinessRuleException (conflict)
     * 4) Create Consultation with state 'SCHEDULED', set doctor and medicalRecord, leave clinical fields null
     * 5) Save and publish ConsultationScheduledEvent with consultation id
     */
    @Override
    @Transactional
    public Consultation scheduleConsultation(ScheduleConsultationRequestDTO dto) throws Exception {
        // 1) Resolve medical record by patient id
        MedicalRecord mr = medicalRecordRepository.findByPatientId(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord not found for patient id: " + dto.getPatientId()));

        // 2) Lock doctor resource
        Doctor doctor = doctorRepository.findByIdForUpdate(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        // 3) Check overlapping consultations within [requested, requested + 30min)
        OffsetDateTime from = dto.getConsultationDate();
        OffsetDateTime to = from.plusMinutes(30);

        List<Consultation> overlaps = consultationRepository.findOverlappingForDoctorForUpdate(doctor.getId(), from, to);
        if (overlaps != null && !overlaps.isEmpty()) {
            throw new BusinessRuleException("Requested time slot is not available for doctor id: " + doctor.getId());
        }

        // 4) Instantiate Consultation with state SCHEDULED
        Consultation c = new Consultation();
        c.setConsultationDate(from);
        c.setStatus("SCHEDULED");
        c.setDoctor(doctor);
        c.setMedicalRecord(mr);

        Consultation saved = consultationRepository.save(c);

        // 5) Publish event
        eventPublisher.publishEvent(new ConsultationScheduledEvent(this, saved.getId()));

        return saved;
    }
}
