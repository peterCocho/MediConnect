package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.entity.User;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.MedicalRecordRepository;
import com.sena.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentServiceImpl.
 *
 * Cubre las reglas de negocio de HU de agendamiento (ÉPICA 3):
 * horario de atención, domingos, solapamientos de paciente/médico,
 * orden de adquisición del bloqueo pesimista sobre el médico, y las
 * transiciones de estado de confirmación/cancelación.
 *
 * Nota: estos tests verifican la LÓGICA con repositorios mockeados.
 * La prueba real de concurrencia (N hilos reservando el mismo bloque,
 * solo 1 persiste) requiere un test de integración con
 * Testcontainers/Postgres real, ya que PESSIMISTIC_WRITE solo tiene
 * efecto contra una base de datos real.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentServiceImpl")
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private ConsultationRepository consultationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient activePatient;
    private Doctor activeDoctor;
    private MedicalRecord medicalRecord;

    // Lunes 15 de junio de 2026, 09:00-09:30 America/Bogota (UTC-05:00)
    private final OffsetDateTime validStart =
            OffsetDateTime.of(2026, 6, 15, 9, 0, 0, 0, ZoneOffset.of("-05:00"));
    private final OffsetDateTime validEnd =
            OffsetDateTime.of(2026, 6, 15, 9, 30, 0, 0, ZoneOffset.of("-05:00"));

    @BeforeEach
    void setUp() {
        activePatient = Patient.builder().id(17L).fullName("Pedro Contreras").isActive(true).build();

        User doctorUser = User.builder().id(4L).username("dr.garcia").isActive(true).build();
        activeDoctor = Doctor.builder().id(4L).fullName("Dr. Andrés García")
                .specialty("Dermatología").user(doctorUser).build();

        medicalRecord = MedicalRecord.builder().id(200L).patient(activePatient).build();
    }

    private void stubHappyPathUntilLock() {
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));
        when(appointmentRepository.hasPatientOverlappingAppointments(17L, validStart, validEnd)).thenReturn(false);
        when(doctorRepository.findByIdWithLock(4L)).thenReturn(Optional.of(activeDoctor));
        when(appointmentRepository.hasOverlappingAppointments(4L, validStart, validEnd)).thenReturn(false);
        when(medicalRecordRepository.findByPatientId(17L)).thenReturn(Optional.of(medicalRecord));
    }

    @Test
    @DisplayName("bookAppointment: reserva válida en horario hábil crea la cita y la consulta enlazada")
    void bookAppointment_withValidSlot_createsAppointmentAndConsultation() {
        stubHappyPathUntilLock();
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(63L);
            return a;
        });
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> {
            Consultation c = invocation.getArgument(0);
            c.setId(500L);
            return c;
        });

        Appointment result = appointmentService.bookAppointment(17L, 4L, validStart, validEnd);

        assertThat(result.getId()).isEqualTo(63L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING_CONFIRMATION);

        verify(eventPublisher).publishEvent(any(com.sena.backend.ConsultationScheduledEvent.class));
        verify(eventPublisher).publishEvent(
                any(com.sena.backend.event.AppointmentRequiresConfirmationEvent.class));
    }

    @Test
    @DisplayName("bookAppointment: el bloqueo del médico se adquiere DESPUÉS de validar al paciente")
    void bookAppointment_acquiresDoctorLock_afterPatientChecks() {
        stubHappyPathUntilLock();
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(inv -> inv.getArgument(0));

        appointmentService.bookAppointment(17L, 4L, validStart, validEnd);

        // Orden crítico para evitar deadlocks: primero se valida/lee al paciente
        // y su solapamiento, y solo después se toma el lock pesimista del médico.
        InOrder inOrder = inOrder(patientRepository, appointmentRepository, doctorRepository);
        inOrder.verify(patientRepository).findById(17L);
        inOrder.verify(appointmentRepository).hasPatientOverlappingAppointments(17L, validStart, validEnd);
        inOrder.verify(doctorRepository).findByIdWithLock(4L);
    }

    @Test
    @DisplayName("bookAppointment: domingo es rechazado sin tocar la base de datos")
    void bookAppointment_onSunday_throwsBusinessRuleException() {
        // Domingo 21 de junio de 2026
        OffsetDateTime sundayStart = OffsetDateTime.of(2026, 6, 21, 9, 0, 0, 0, ZoneOffset.of("-05:00"));
        OffsetDateTime sundayEnd = OffsetDateTime.of(2026, 6, 21, 9, 30, 0, 0, ZoneOffset.of("-05:00"));

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, sundayStart, sundayEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("domingo");

        verifyNoInteractions(patientRepository, doctorRepository, appointmentRepository);
    }

    @Test
    @DisplayName("bookAppointment: antes de las 8:00 AM es rechazado por fuera de horario")
    void bookAppointment_beforeOpeningTime_throwsBusinessRuleException() {
        OffsetDateTime earlyStart = OffsetDateTime.of(2026, 6, 15, 7, 30, 0, 0, ZoneOffset.of("-05:00"));
        OffsetDateTime earlyEnd = OffsetDateTime.of(2026, 6, 15, 8, 0, 0, 0, ZoneOffset.of("-05:00"));

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, earlyStart, earlyEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("jornada de atención");

        verifyNoInteractions(patientRepository, doctorRepository, appointmentRepository);
    }

    @Test
    @DisplayName("bookAppointment: después de las 6:00 PM es rechazado por fuera de horario")
    void bookAppointment_afterClosingTime_throwsBusinessRuleException() {
        OffsetDateTime lateStart = OffsetDateTime.of(2026, 6, 15, 18, 0, 0, 0, ZoneOffset.of("-05:00"));
        OffsetDateTime lateEnd = OffsetDateTime.of(2026, 6, 15, 18, 30, 0, 0, ZoneOffset.of("-05:00"));

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, lateStart, lateEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("jornada de atención");
    }

    @Test
    @DisplayName("bookAppointment: paciente inactivo es rechazado")
    void bookAppointment_withInactivePatient_throwsBusinessRuleException() {
        activePatient.setActive(false);
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactivo");

        verifyNoInteractions(doctorRepository);
    }

    @Test
    @DisplayName("bookAppointment: paciente inexistente lanza ResourceNotFoundException")
    void bookAppointment_withUnknownPatient_throwsResourceNotFound() {
        when(patientRepository.findById(17L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("bookAppointment: solapamiento del paciente es rechazado antes de tocar al médico")
    void bookAppointment_withPatientOverlap_throwsBusinessRuleException() {
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));
        when(appointmentRepository.hasPatientOverlappingAppointments(17L, validStart, validEnd)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("paciente ya tiene una cita");

        verifyNoInteractions(doctorRepository);
    }

    @Test
    @DisplayName("bookAppointment: médico inexistente lanza ResourceNotFoundException")
    void bookAppointment_withUnknownDoctor_throwsResourceNotFound() {
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));
        when(appointmentRepository.hasPatientOverlappingAppointments(17L, validStart, validEnd)).thenReturn(false);
        when(doctorRepository.findByIdWithLock(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("bookAppointment: médico inactivo es rechazado")
    void bookAppointment_withInactiveDoctorUser_throwsBusinessRuleException() {
        activeDoctor.getUser().setActive(false);
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));
        when(appointmentRepository.hasPatientOverlappingAppointments(17L, validStart, validEnd)).thenReturn(false);
        when(doctorRepository.findByIdWithLock(4L)).thenReturn(Optional.of(activeDoctor));

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("médico se encuentra inactivo");

        verify(appointmentRepository, never()).hasOverlappingAppointments(anyLong(), any(), any());
    }

    @Test
    @DisplayName("bookAppointment: solapamiento del médico (bajo el lock) es rechazado sin persistir nada")
    void bookAppointment_withDoctorOverlap_throwsBusinessRuleException() {
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));
        when(appointmentRepository.hasPatientOverlappingAppointments(17L, validStart, validEnd)).thenReturn(false);
        when(doctorRepository.findByIdWithLock(4L)).thenReturn(Optional.of(activeDoctor));
        when(appointmentRepository.hasOverlappingAppointments(4L, validStart, validEnd)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("médico seleccionado ya tiene una cita");

        verify(appointmentRepository, never()).save(any());
        verifyNoInteractions(consultationRepository, eventPublisher);
    }

    @Test
    @DisplayName("bookAppointment: sin expediente médico lanza ResourceNotFoundException")
    void bookAppointment_withoutMedicalRecord_throwsResourceNotFound() {
        when(patientRepository.findById(17L)).thenReturn(Optional.of(activePatient));
        when(appointmentRepository.hasPatientOverlappingAppointments(17L, validStart, validEnd)).thenReturn(false);
        when(doctorRepository.findByIdWithLock(4L)).thenReturn(Optional.of(activeDoctor));
        when(appointmentRepository.hasOverlappingAppointments(4L, validStart, validEnd)).thenReturn(false);
        when(medicalRecordRepository.findByPatientId(17L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(17L, 4L, validStart, validEnd))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
    }

    // ---- confirmAppointment ----

    @Test
    @DisplayName("confirmAppointment: transiciona de PENDING_CONFIRMATION a SCHEDULED y propaga a la consulta")
    void confirmAppointment_fromPending_transitionsToScheduled() {
        Appointment pending = Appointment.builder().id(63L).status(AppointmentStatus.PENDING_CONFIRMATION).build();
        Consultation linked = Consultation.builder().id(500L).status(AppointmentStatus.PENDING_CONFIRMATION).build();

        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(pending));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consultationRepository.findByAppointmentId(63L)).thenReturn(Optional.of(linked));

        Appointment result = appointmentService.confirmAppointment(63L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(consultationRepository).save(argThat(c -> c.getStatus() == AppointmentStatus.SCHEDULED));
    }

    @Test
    @DisplayName("confirmAppointment: es idempotente si ya está SCHEDULED (no lanza error, no vuelve a guardar)")
    void confirmAppointment_alreadyScheduled_isIdempotent() {
        Appointment scheduled = Appointment.builder().id(63L).status(AppointmentStatus.SCHEDULED).build();
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(scheduled));

        Appointment result = appointmentService.confirmAppointment(63L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(appointmentRepository, never()).save(any());
        verifyNoInteractions(consultationRepository);
    }

    @Test
    @DisplayName("confirmAppointment: una cita CANCELED no puede confirmarse")
    void confirmAppointment_whenCanceled_throwsBusinessRuleException() {
        Appointment canceled = Appointment.builder().id(63L).status(AppointmentStatus.CANCELED).build();
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(canceled));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(63L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CANCELED");
    }

    // ---- cancelAppointment ----

    @Test
    @DisplayName("cancelAppointment: cancela una cita PENDING_CONFIRMATION y publica el evento")
    void cancelAppointment_fromPending_succeedsAndPublishesEvent() {
        Appointment pending = Appointment.builder().id(63L).status(AppointmentStatus.PENDING_CONFIRMATION).build();
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(pending));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consultationRepository.findByAppointmentId(63L)).thenReturn(Optional.empty());

        Appointment result = appointmentService.cancelAppointment(63L, "Paciente no puede asistir");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(result.getCancellationReason()).isEqualTo("Paciente no puede asistir");
        verify(eventPublisher).publishEvent(any(com.sena.backend.event.AppointmentCanceledEvent.class));
    }

    @Test
    @DisplayName("cancelAppointment: una cita ya CANCELED no puede volver a cancelarse")
    void cancelAppointment_alreadyCanceled_throwsBusinessRuleException() {
        Appointment canceled = Appointment.builder().id(63L).status(AppointmentStatus.CANCELED).build();
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(canceled));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(63L, "motivo"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya se encuentra cancelada");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelAppointment: una cita COMPLETED no puede cancelarse")
    void cancelAppointment_whenCompleted_throwsBusinessRuleException() {
        Appointment completed = Appointment.builder().id(63L).status(AppointmentStatus.COMPLETED).build();
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(63L, "motivo"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue completada");
    }
}
