package com.sena.backend.service;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import org.springframework.data.domain.Page;

public interface PatientService {
    PatientResponse createPatient(CreatePatientRequest req);
    PatientResponse getPatientById(Long id);
    PatientResponse getPatientByPhone(String phone);
    Page<PatientResponse> getAllPatients(int page, int size, String sortBy);
    PatientResponse updatePatient(Long id, UpdatePatientRequest req);
    PatientResponse togglePatientStatus(Long id, boolean status);
}