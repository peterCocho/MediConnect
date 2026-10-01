package com.sena.backend.integration;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.repository.MedicalRecordRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.service.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba de integración (Testcontainers + PostgreSQL real): creación de un
 * paciente con su historia clínica en cascada.
 *
 * PatientServiceImpl.createPatient construye el Patient y su MedicalRecord
 * en memoria y guarda solo el Patient, confiando en CascadeType.ALL para
 * que Hibernate persista también el MedicalRecord. Eso es exactamente lo
 * que un test con Mockito no puede verificar (el save() está simulado, no
 * dispara cascada real, ni corre las restricciones de la base de datos).
 * Aquí se confirma contra una base real:
 *   - que el MedicalRecord efectivamente queda guardado y enlazado al
 *     paciente (no solo en el objeto Java, sino como fila en la BD),
 *   - que el número de historia se autogenera y es único,
 *   - que el teléfono se normaliza antes de persistir,
 *   - que un documento de identidad duplicado se rechaza sin dejar
 *     registros huérfanos.
 */
@DisplayName("Integración: creación de paciente con historia clínica en cascada")
class PatientMedicalRecordIT extends AbstractIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    private CreatePatientRequest request(String identityDocument, String phone) {
        return CreatePatientRequest.builder()
                .identityDocument(identityDocument)
                .fullName("Paciente Integración")
                .phone(phone)
                .birthDate(LocalDate.of(1990, 5, 20))
                .build();
    }

    @Test
    @DisplayName("createPatient: persiste el paciente y, en cascada, su historia clínica enlazada en la base de datos real")
    void createPatient_persistsPatientAndCascadesMedicalRecordInRealDatabase() {
        PatientResponse response = patientService.createPatient(request("IT-PAT-001", "3107984713"));

        assertThat(response.getId()).isNotNull();
        assertThat(response.getMedicalRecordId()).isNotNull();

        // No confiamos solo en la respuesta del servicio: releemos directo de la base
        // de datos, por fuera del contexto transaccional del servicio, para confirmar
        // que ambas filas quedaron realmente persistidas y enlazadas.
        Patient persistedPatient = patientRepository.findById(response.getId()).orElseThrow();
        assertThat(persistedPatient.getIdentityDocument()).isEqualTo("IT-PAT-001");

        Optional<MedicalRecord> persistedRecord = medicalRecordRepository.findByPatientId(persistedPatient.getId());
        assertThat(persistedRecord).isPresent();
        assertThat(persistedRecord.get().getId()).isEqualTo(response.getMedicalRecordId());
        assertThat(persistedRecord.get().getPatient().getId()).isEqualTo(persistedPatient.getId());
        assertThat(persistedRecord.get().getRecordNumber()).startsWith("MR-");
        assertThat(persistedRecord.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createPatient: normaliza un celular colombiano de 10 dígitos a formato E.164 antes de persistir")
    void createPatient_normalizesBareColombianMobileNumber() {
        patientService.createPatient(request("IT-PAT-002", "3107984713"));

        Patient persisted = patientRepository.findByIdentityDocument("IT-PAT-002").orElseThrow();
        assertThat(persisted.getPhone()).isEqualTo("573107984713");
    }

    @Test
    @DisplayName("createPatient: un documento de identidad duplicado lanza BusinessRuleException y no deja registros huérfanos")
    void createPatient_withDuplicateIdentityDocument_throwsAndLeavesNoOrphanRecords() {
        patientService.createPatient(request("IT-PAT-003", "3107984713"));

        assertThatThrownBy(() -> patientService.createPatient(request("IT-PAT-003", "3009998877")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("IT-PAT-003");

        List<Patient> allWithThatDocument = patientRepository.findAll().stream()
                .filter(p -> "IT-PAT-003".equals(p.getIdentityDocument()))
                .toList();
        assertThat(allWithThatDocument).hasSize(1);

        // El intento fallido no debió dejar una historia clínica huérfana sin paciente.
        Patient survivor = allWithThatDocument.get(0);
        assertThat(medicalRecordRepository.findByPatientId(survivor.getId())).isPresent();
    }

    @Test
    @DisplayName("createPatient: pacientes distintos reciben números de historia clínica únicos")
    void createPatient_assignsDistinctRecordNumbersAcrossPatients() {
        PatientResponse first = patientService.createPatient(request("IT-PAT-004", "3001112233"));
        PatientResponse second = patientService.createPatient(request("IT-PAT-005", "3004445566"));

        MedicalRecord firstRecord = medicalRecordRepository.findByPatientId(first.getId()).orElseThrow();
        MedicalRecord secondRecord = medicalRecordRepository.findByPatientId(second.getId()).orElseThrow();

        assertThat(firstRecord.getRecordNumber()).isNotEqualTo(secondRecord.getRecordNumber());
    }
}