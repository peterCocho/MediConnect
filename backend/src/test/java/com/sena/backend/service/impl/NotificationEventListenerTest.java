package com.sena.backend.service.impl;

import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Notification;
import com.sena.backend.entity.Patient;
import com.sena.backend.event.AppointmentCanceledEvent;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.NotificationRepository;
import com.sena.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NotificationEventListener.
 *
 * Los listeners se invocan directamente (sin contexto Spring), por lo que
 * aquí se prueba la lógica de negocio y no el comportamiento transaccional
 * ni asíncrono. Regla clave: la confirmación inmediata solo se registra si
 * la cita está a más de 48 horas; la cancelación siempre se registra. Las
 * notificaciones se guardan como LOGGED (auditoría local, sin envío HTTP).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener")
class NotificationEventListenerTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificationEventListener listener;

    private Patient patient;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        patient = Patient.builder().id(17L).identityDocument("1094123456")
                .fullName("Pedro Contreras").phone("573107984713").isActive(true).build();
        medicalRecord = MedicalRecord.builder().id(200L).patient(patient).build();
    }

    private Consultation consultationAt(OffsetDateTime date) {
        return Consultation.builder()
                .id(500L)
                .status(AppointmentStatus.SCHEDULED)
                .consultationDate(date)
                .medicalRecord(medicalRecord)
                .build();
    }

    private ConsultationScheduledEvent scheduledEvent() {
        return new ConsultationScheduledEvent(this, 500L);
    }

    private AppointmentCanceledEvent canceledEvent() {
        return new AppointmentCanceledEvent(this, 63L);
    }

    // ------------------------------------------------------------------
    // handleConsultationScheduled
    // ------------------------------------------------------------------

    @Test
    @DisplayName("handleConsultationScheduled: cita a más de 48 h registra una notificación CONFIRMATION en estado LOGGED")
    void handleConsultationScheduled_whenMoreThan48Hours_savesConfirmation() {
        Consultation consultation = consultationAt(OffsetDateTime.now().plusDays(3));
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));

        listener.handleConsultationScheduled(scheduledEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo("CONFIRMATION");
        assertThat(saved.getStatus()).isEqualTo("LOGGED");
        assertThat(saved.getDestinationNumber()).isEqualTo("573107984713");
        assertThat(saved.getConsultation()).isSameAs(consultation);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("handleConsultationScheduled: cita a menos de 48 h no registra confirmación (lo cubre el scheduler de 24 h)")
    void handleConsultationScheduled_whenLessThan48Hours_doesNotSave() {
        Consultation consultation = consultationAt(OffsetDateTime.now().plusDays(1));
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));

        listener.handleConsultationScheduled(scheduledEvent());

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleConsultationScheduled: consulta inexistente no registra nada")
    void handleConsultationScheduled_withUnknownConsultation_doesNothing() {
        when(consultationRepository.findById(500L)).thenReturn(Optional.empty());

        listener.handleConsultationScheduled(scheduledEvent());

        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("handleConsultationScheduled: paciente con teléfono vacío no genera notificación")
    void handleConsultationScheduled_whenPatientPhoneBlank_doesNotSave() {
        patient.setPhone("  ");
        Consultation consultation = consultationAt(OffsetDateTime.now().plusDays(3));
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));

        listener.handleConsultationScheduled(scheduledEvent());

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleConsultationScheduled: consulta sin historia clínica no genera notificación")
    void handleConsultationScheduled_whenNoMedicalRecord_doesNotSave() {
        Consultation consultation = consultationAt(OffsetDateTime.now().plusDays(3));
        consultation.setMedicalRecord(null);
        when(consultationRepository.findById(500L)).thenReturn(Optional.of(consultation));

        listener.handleConsultationScheduled(scheduledEvent());

        verify(notificationRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // handleAppointmentCanceled
    // ------------------------------------------------------------------

    @Test
    @DisplayName("handleAppointmentCanceled: registra una notificación CANCELLATION en estado LOGGED para el paciente")
    void handleAppointmentCanceled_withLinkedConsultation_savesCancellation() {
        Consultation consultation = consultationAt(OffsetDateTime.now().plusHours(5));
        Appointment appointment = Appointment.builder().id(63L).status(AppointmentStatus.CANCELED).build();
        appointment.setConsultation(consultation);
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(appointment));

        listener.handleAppointmentCanceled(canceledEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getType()).isEqualTo("CANCELLATION");
        assertThat(saved.getStatus()).isEqualTo("LOGGED");
        assertThat(saved.getDestinationNumber()).isEqualTo("573107984713");
        assertThat(saved.getConsultation()).isSameAs(consultation);
    }

    @Test
    @DisplayName("handleAppointmentCanceled: cita inexistente no registra nada")
    void handleAppointmentCanceled_withUnknownAppointment_doesNothing() {
        when(appointmentRepository.findById(63L)).thenReturn(Optional.empty());

        listener.handleAppointmentCanceled(canceledEvent());

        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("handleAppointmentCanceled: cita sin consulta asociada no registra nada")
    void handleAppointmentCanceled_withoutLinkedConsultation_doesNothing() {
        Appointment appointment = Appointment.builder().id(63L).status(AppointmentStatus.CANCELED).build();
        when(appointmentRepository.findById(63L)).thenReturn(Optional.of(appointment));

        listener.handleAppointmentCanceled(canceledEvent());

        verifyNoInteractions(notificationRepository);
    }
}
