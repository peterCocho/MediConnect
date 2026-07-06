package com.sena.backend.service;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import org.springframework.data.domain.Page;

public interface PatientService {
    void createPatient(CreatePatientRequest req);
    PatientResponse getPatientById(Long id);
    Page<PatientResponse> getAllPatients(int page, int size, String sortBy);
    void updatePatient(Long id, UpdatePatientRequest req);
    void togglePatientStatus(Long id, boolean status);
}