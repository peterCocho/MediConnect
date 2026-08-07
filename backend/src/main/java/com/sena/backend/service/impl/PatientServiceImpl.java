package com.sena.backend.service.impl;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public PatientResponse createPatient(CreatePatientRequest req) {
        if (patientRepository.findByIdentityDocument(req.getIdentityDocument()).isPresent()) {
            throw new BusinessRuleException("Ya existe un paciente registrado con el documento: " + req.getIdentityDocument());
        }

        // 1. Build the patient entity
        Patient patient = Patient.builder()
                .identityDocument(req.getIdentityDocument())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .birthDate(req.getBirthDate())
                .isActive(true)
                .build();

// 2 & 3. Provision the empty medical record using Builder
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .patient(patient)
                // TODO: Assign any other non-null required fields here (like recordNumber)
                .build();

        patient.setMedicalRecord(medicalRecord);

        // Save triggers cascade persistence for MedicalRecord (assuming CascadeType.ALL is configured in Patient entity)
        Patient savedPatient = patientRepository.save(patient);

        return mapToResponse(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        return mapToResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return patientRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(Long id, UpdatePatientRequest req) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        patient.setFullName(req.getFullName());
        patient.setPhone(req.getPhone());
        patient.setBirthDate(req.getBirthDate());

        if (req.getIsActive() != null) {
            patient.setActive(req.getIsActive());
        }

        return mapToResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientResponse togglePatientStatus(Long id, boolean status) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        // Validation for deactivation scenario
        if (!status) {
            boolean hasPendingAppointments = appointmentRepository.existsByPatientIdAndStatus(id, "SCHEDULED");

            if (hasPendingAppointments) {
                throw new BusinessRuleException("No se puede desactivar al paciente porque tiene citas programadas pendientes. Cancélelas primero.");
            }
        }

        patient.setActive(status);
        return mapToResponse(patientRepository.save(patient));
    }

    private PatientResponse mapToResponse(Patient patient) {
        // Safe extraction of medicalRecordId to prevent NullPointerExceptions on legacy records
        Long recordId = (patient.getMedicalRecord() != null) ? patient.getMedicalRecord().getId() : null;

        return PatientResponse.builder()
                .id(patient.getId())
                .identityDocument(patient.getIdentityDocument())
                .fullName(patient.getFullName())
                .phone(patient.getPhone())
                .birthDate(patient.getBirthDate())
                .isActive(patient.getIsActive())
                .medicalRecordId(recordId) // Propagated to DTO
                .build();
    }
}