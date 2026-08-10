package com.sena.backend.service.impl;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void createPatientCreatesMedicalRecordWithRecordNumberAndCreatedAt() {
        when(patientRepository.findByIdentityDocument(anyString())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        patientService.createPatient(createPatientRequest("1001", "Ana Perez"));

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());

        Patient savedPatient = patientCaptor.getValue();
        MedicalRecord medicalRecord = savedPatient.getMedicalRecord();

        assertThat(medicalRecord).isNotNull();
        assertThat(medicalRecord.getPatient()).isSameAs(savedPatient);
        assertThat(medicalRecord.getRecordNumber()).isNotBlank();
        assertThat(medicalRecord.getCreatedAt()).isNotNull();
    }

    @Test
    void createPatientGeneratesDifferentRecordNumbersForDifferentPatients() {
        when(patientRepository.findByIdentityDocument(anyString())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        patientService.createPatient(createPatientRequest("1001", "Ana Perez"));
        patientService.createPatient(createPatientRequest("1002", "Luis Gomez"));

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository, times(2)).save(patientCaptor.capture());

        String firstRecordNumber = patientCaptor.getAllValues().get(0).getMedicalRecord().getRecordNumber();
        String secondRecordNumber = patientCaptor.getAllValues().get(1).getMedicalRecord().getRecordNumber();

        assertThat(firstRecordNumber).isNotBlank();
        assertThat(secondRecordNumber).isNotBlank();
        assertThat(firstRecordNumber).isNotEqualTo(secondRecordNumber);
    }

    private CreatePatientRequest createPatientRequest(String document, String fullName) {
        CreatePatientRequest request = new CreatePatientRequest();
        request.setIdentityDocument(document);
        request.setFullName(fullName);
        request.setPhone("3001234567");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        return request;
    }
}
