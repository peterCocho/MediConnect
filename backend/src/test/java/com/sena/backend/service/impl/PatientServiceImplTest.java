package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PatientServiceImpl.
 *
 * Cubre HU-03 (Registro de Paciente y Aprovisionamiento de Expediente):
 * creación con historia clínica vinculada, unicidad del documento,
 * actualización y activación/desactivación con validación de citas pendientes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientServiceImpl")
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private CreatePatientRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = CreatePatientRequest.builder()
                .identityDocument("1094123456")
                .fullName("Pablo Vega")
                .phone("310 798 4713")
                .birthDate(LocalDate.of(1998, 5, 20))
                .build();
    }

    @Test
    @DisplayName("createPatient: crea el paciente con su historia clínica vinculada (1:1)")
    void createPatient_withNewDocument_provisionsMedicalRecordAndReturnsResponse() {
        when(patientRepository.findByIdentityDocument("1094123456")).thenReturn(Optional.empty());

        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            p.setId(1L);
            if (p.getMedicalRecord() != null) {
                p.getMedicalRecord().setId(99L);
            }
            return p;
        });

        PatientResponse response = patientService.createPatient(createRequest);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient persisted = captor.getValue();

        // Transaccionalidad crítica: el expediente médico debe quedar vinculado
        // al paciente ANTES de persistir (misma transacción / cascada).
        assertThat(persisted.getMedicalRecord()).isNotNull();
        assertThat(persisted.getMedicalRecord().getPatient()).isSameAs(persisted);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMedicalRecordId()).isEqualTo(99L);
        assertThat(response.getFullName()).isEqualTo("Pablo Vega");
    }

    @Test
    @DisplayName("createPatient: normaliza un celular colombiano de 10 dígitos anteponiendo 57")
    void createPatient_normalizesTenDigitColombianMobile() {
        when(patientRepository.findByIdentityDocument(anyString())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createRequest.setPhone("310-798-4713");
        patientService.createPatient(createRequest);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());

        assertThat(captor.getValue().getPhone()).isEqualTo("573107984713");
    }

    @Test
    @DisplayName("createPatient: documento duplicado lanza BusinessRuleException (409) y no guarda nada")
    void createPatient_withDuplicateDocument_throwsBusinessRuleException() {
        Patient existing = Patient.builder().id(5L).identityDocument("1094123456").build();
        when(patientRepository.findByIdentityDocument("1094123456")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> patientService.createPatient(createRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("1094123456");

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPatientById: paciente inexistente lanza ResourceNotFoundException")
    void getPatientById_withUnknownId_throwsResourceNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updatePatient: actualiza los campos editables y normaliza el teléfono")
    void updatePatient_updatesEditableFields() {
        Patient existing = Patient.builder()
                .id(1L)
                .identityDocument("1094123456")
                .fullName("Nombre Viejo")
                .phone("573107984713")
                .birthDate(LocalDate.of(1998, 5, 20))
                .isActive(true)
                .build();

        UpdatePatientRequest updateRequest = UpdatePatientRequest.builder()
                .fullName("Pedro Pablo Contreras Vega")
                .phone("300 111 2222")
                .birthDate(LocalDate.of(1998, 5, 21))
                .isActive(null)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientService.updatePatient(1L, updateRequest);

        assertThat(response.getFullName()).isEqualTo("Pedro Pablo Contreras Vega");
        assertThat(response.getPhone()).isEqualTo("573001112222");
        // isActive == null en el request no debe tocar el estado actual
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("togglePatientStatus: desactivar con citas pendientes o programadas lanza BusinessRuleException")
    void togglePatientStatus_deactivateWithPendingAppointments_throwsBusinessRuleException() {
        Patient existing = Patient.builder().id(1L).isActive(true).build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.existsByPatientIdAndStatusIn(
                eq(1L),
                eq(List.of(AppointmentStatus.PENDING_CONFIRMATION, AppointmentStatus.SCHEDULED))
        )).thenReturn(true);

        assertThatThrownBy(() -> patientService.togglePatientStatus(1L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("citas programadas");

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("togglePatientStatus: desactivar sin citas pendientes actualiza el estado")
    void togglePatientStatus_deactivateWithoutPendingAppointments_succeeds() {
        Patient existing = Patient.builder().id(1L).isActive(true).build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.existsByPatientIdAndStatusIn(eq(1L), anyList())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientService.togglePatientStatus(1L, false);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("togglePatientStatus: reactivar un paciente nunca valida citas pendientes")
    void togglePatientStatus_reactivate_doesNotCheckPendingAppointments() {
        Patient existing = Patient.builder().id(1L).isActive(false).build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientService.togglePatientStatus(1L, true);

        assertThat(response.isActive()).isTrue();
        verifyNoInteractions(appointmentRepository);
    }
}
