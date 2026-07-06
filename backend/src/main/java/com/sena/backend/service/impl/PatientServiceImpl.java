// Archivo 2: src/main/java/com/sena/backend/service/impl/PatientServiceImpl.java
package com.sena.backend.service.impl;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import com.sena.backend.entity.Patient;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public void createPatient(CreatePatientRequest req) {
        if (patientRepository.findByIdentityDocument(req.getIdentityDocument()).isPresent()) {
            throw new BusinessRuleException("Ya existe un paciente registrado con el documento: " + req.getIdentityDocument());
        }

        Patient patient = Patient.builder()
                .identityDocument(req.getIdentityDocument())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .birthDate(req.getBirthDate())
                .isActive(true) // Activo por defecto en la creación
                .build();

        patientRepository.save(patient);
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
    public void updatePatient(Long id, UpdatePatientRequest req) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        patient.setFullName(req.getFullName());
        patient.setPhone(req.getPhone());
        patient.setBirthDate(req.getBirthDate());

        if (req.getIsActive() != null) {
            patient.setActive(req.getIsActive());
        }

        patientRepository.save(patient);
    }

    @Override
    @Transactional
    public void togglePatientStatus(Long id, boolean status) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        patient.setActive(status);
        patientRepository.save(patient);
    }

    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .identityDocument(patient.getIdentityDocument())
                .fullName(patient.getFullName())
                .phone(patient.getPhone())
                .birthDate(patient.getBirthDate())
                .isActive(patient.isActive())
                .build();
    }
}
