package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.consultation.ConsultationResponseDTO;
import com.sena.backend.domain.consultation.ExecuteConsultationRequestDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConsultationServiceImpl.
 *
 * Cubre HU-06 (Ejecución de Consulta Clínica) y HU-07 (Historial Clínico
 * Inmutable): solo consultas en estado SCHEDULED pueden ejecutarse, la
 * ejecución sella el registro como COMPLETED y sincroniza la cita asociada,
 * y se protege contra IDOR (un médico no puede ejecutar/ver consultas que
 * no le pertenecen).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultationServiceImpl")
class ConsultationServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private Doctor assignedDoctor;
    private Doctor otherDoctor;
    private Patient patient;
    private MedicalRecord medicalRecord;
    private Appointment linkedAppointment;
    private ExecuteConsultationRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        assignedDoctor = Doctor.builder().id(4L).fullName("Dr. Andrés García").specialty("Dermatología").build();
        otherDoctor = Doctor.builder().id(9L).fullName("Dra. Otra Persona").specialty("Pediatría").build();

        patient = Patient.builder().id(17L).identityDocument("1094123456")
                .fullName("Pedro Contreras").phone("573107984713").isActive(true).build();

        medicalRecord = MedicalRecord.builder().id(200L).patient(patient).build();

        linkedAppointment = Appointment.builder().id(63L).status(AppointmentStatus.SCHEDULED).build();

        validRequest = new ExecuteConsultationRequestDTO();
        validRequest.setSystolicPressure(120);
        validRequest.setDiastolicPressure(80);
        validRequest.setHeartRate(72);
        validRequest.setWeight(new BigDecimal("70.50"));
        validRequest.setIcd10Code("J06.9");
        validRequest.setReasonForVisit("Control de rutina");
        validRequest.setClinicalNotes("Sin hallazgos relevantes");
        validRequest.setManagementPlan("Seguimiento en 6 meses");
    }

    private Consultation scheduledConsultation() {
        return Consultation.builder()
                .id(500L)
                .status(AppointmentStatus.SCHEDULED)
                .doctor(assignedDoctor)
                .medicalRecord(medicalRecord)
                .appointment(linkedAppointment)
                .consultationDate(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("executeConsultation: con datos válidos sella la consulta como COMPLETED y sincroniza la cita")
    void executeConsultation_withValidData_sealsConsultationAndSyncsAppointment() {
        Consultation consultation = scheduledConsultation();
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(assignedDoctor));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultationResponseDTO response = consultationService.executeConsultation(500L, validRequest, 4L);

        ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationRepository).save(captor.capture());
        Consultation saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(saved.getAppointment().getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(saved.getSystolicPressure()).isEqualTo(120);
        assertThat(saved.getIcd10Code()).isEqualTo("J06.9");

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getPatientId()).isEqualTo(17L);
        assertThat(response.getFullName()).isEqualTo("Pedro Contreras");
    }

    @Test
    @DisplayName("executeConsultation: una consulta que no está SCHEDULED no puede ejecutarse (inmutabilidad)")
    void executeConsultation_whenNotScheduled_throwsBusinessRuleException() {
        Consultation alreadyCompleted = scheduledConsultation();
        alreadyCompleted.setStatus(AppointmentStatus.COMPLETED);

        when(consultationRepository.findById(500L)).thenReturn(Optional.of(alreadyCompleted));
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(assignedDoctor));

        assertThatThrownBy(() -> consultationService.executeConsultation(500L, validRequest, 4L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("SCHEDULED");

        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("executeConsultation: un médico distinto al asignado no puede ejecutar la consulta (IDOR)")
    void executeConsultation_withDifferentDoctor_throwsAccessDenied() {
        Consultation consultation = scheduledConsultation(); // asignada a assignedDoctor (id=4)
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));
        when(doctorRepository.findById(9L)).thenReturn(Optional.of(otherDoctor));

        assertThatThrownBy(() -> consultationService.executeConsultation(500L, validRequest, 9L))
                .isInstanceOf(AccessDeniedException.class);

        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("executeConsultation: consulta inexistente lanza ResourceNotFoundException")
    void executeConsultation_withUnknownConsultation_throwsResourceNotFound() {
        when(consultationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationService.executeConsultation(999L, validRequest, 4L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(doctorRepository);
    }

    @Test
    @DisplayName("executeConsultation: perfil de médico inexistente lanza ResourceNotFoundException")
    void executeConsultation_withUnknownDoctor_throwsResourceNotFound() {
        Consultation consultation = scheduledConsultation();
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationService.executeConsultation(500L, validRequest, 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPatientTimeline: solo retorna consultas COMPLETED ordenadas por fecha descendente")
    void getPatientTimeline_returnsOnlyCompletedConsultationsMapped() {
        Consultation completed = scheduledConsultation();
        completed.setStatus(AppointmentStatus.COMPLETED);

        when(consultationRepository.findByMedicalRecordIdAndStatusOrderByConsultationDateDesc(200L, AppointmentStatus.COMPLETED))
                .thenReturn(List.of(completed));

        List<ConsultationResponseDTO> timeline = consultationService.getPatientTimeline(200L);

        assertThat(timeline).hasSize(1);
        assertThat(timeline.get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(timeline.get(0).getMedicalRecordId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("getConsultationByIdAndDoctorId: acceso de un médico distinto es denegado")
    void getConsultationByIdAndDoctorId_withDifferentDoctor_throwsAccessDenied() {
        Consultation consultation = scheduledConsultation(); // doctor id = 4
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));

        assertThatThrownBy(() -> consultationService.getConsultationByIdAndDoctorId(500L, 9L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getConsultationByIdAndDoctorId: el médico dueño de la consulta sí puede consultarla")
    void getConsultationByIdAndDoctorId_withOwningDoctor_returnsDTO() {
        Consultation consultation = scheduledConsultation();
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));

        ConsultationResponseDTO response = consultationService.getConsultationByIdAndDoctorId(500L, 4L);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getDoctorId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("getConsultationsByDoctorId: delega la paginación al repositorio y mapea cada resultado")
    void getConsultationsByDoctorId_mapsPagedResults() {
        Consultation consultation = scheduledConsultation();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Consultation> page = new PageImpl<>(List.of(consultation), pageable, 1);

        when(consultationRepository.findByDoctorId(eq(4L), eq(pageable))).thenReturn(page);

        Page<ConsultationResponseDTO> result = consultationService.getConsultationsByDoctorId(4L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDoctorId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("getCompletedConsultationsByDoctorId: filtra por estado COMPLETED a nivel de repositorio")
    void getCompletedConsultationsByDoctorId_filtersByCompletedStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Consultation> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(consultationRepository.findByDoctorIdAndStatus(4L, AppointmentStatus.COMPLETED, pageable))
                .thenReturn(emptyPage);

        Page<ConsultationResponseDTO> result = consultationService.getCompletedConsultationsByDoctorId(4L, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(consultationRepository).findByDoctorIdAndStatus(4L, AppointmentStatus.COMPLETED, pageable);
    }
}
