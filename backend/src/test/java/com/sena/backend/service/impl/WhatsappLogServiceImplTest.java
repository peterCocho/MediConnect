package com.sena.backend.service.impl;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.integration.WhatsappMessageLogDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Patient;
import com.sena.backend.entity.WhatsappMessageLog;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.repository.WhatsappMessageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WhatsappLogServiceImpl.
 *
 * Cubre la bandeja de mensajes de WhatsApp sin leer que ve la recepcionista:
 * cada mensaje se enriquece con el nombre del paciente (buscando el teléfono
 * con y sin el prefijo '+') y con las especialidades de sus citas en estado
 * PENDING_CONFIRMATION; además cubre el marcado como leído.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WhatsappLogServiceImpl")
class WhatsappLogServiceImplTest {

    @Mock
    private WhatsappMessageLogRepository messageLogRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private WhatsappLogServiceImpl whatsappLogService;

    private Patient patient;
    private OffsetDateTime receivedAt;

    @BeforeEach
    void setUp() {
        patient = Patient.builder().id(17L).identityDocument("1094123456")
                .fullName("Pedro Contreras").phone("573107984713").isActive(true).build();
        receivedAt = OffsetDateTime.now().minusMinutes(10);
    }

    private WhatsappMessageLog messageFrom(String phone) {
        return WhatsappMessageLog.builder()
                .id(1L)
                .phoneNumber(phone)
                .messageBody("Quiero confirmar mi cita")
                .receivedAt(receivedAt)
                .isRead(false)
                .build();
    }

    private Appointment pendingAppointment(Long id, Long patientId, Long doctorId) {
        return Appointment.builder().id(id).patientId(patientId).doctorId(doctorId)
                .status(AppointmentStatus.PENDING_CONFIRMATION).build();
    }

    // ------------------------------------------------------------------
    // getUnreadMessages
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getUnreadMessages: enriquece el mensaje con el nombre del paciente y las especialidades únicas de sus citas pendientes")
    void getUnreadMessages_withKnownPatient_returnsNameAndDistinctSpecialties() {
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc())
                .thenReturn(List.of(messageFrom("573107984713")));
        when(patientRepository.findByPhone("573107984713")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION)).thenReturn(List.of(
                pendingAppointment(1L, 17L, 4L),
                pendingAppointment(2L, 17L, 4L),   // mismo médico: la especialidad no se repite
                pendingAppointment(3L, 17L, 9L),
                pendingAppointment(4L, 99L, 4L))); // otro paciente: se ignora
        when(doctorRepository.findById(4L))
                .thenReturn(Optional.of(Doctor.builder().id(4L).specialty("Dermatología").build()));
        when(doctorRepository.findById(9L))
                .thenReturn(Optional.of(Doctor.builder().id(9L).specialty("Pediatría").build()));

        List<WhatsappMessageLogDTO> result = whatsappLogService.getUnreadMessages();

        assertThat(result).hasSize(1);
        WhatsappMessageLogDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPhoneNumber()).isEqualTo("573107984713");
        assertThat(dto.getMessageBody()).isEqualTo("Quiero confirmar mi cita");
        assertThat(dto.getReceivedAt()).isEqualTo(receivedAt);
        assertThat(dto.getPatientName()).isEqualTo("Pedro Contreras");
        assertThat(dto.getSpecialty()).isEqualTo("Dermatología, Pediatría");
    }

    @Test
    @DisplayName("getUnreadMessages: si el teléfono llega sin '+' y no hay coincidencia, reintenta la búsqueda con el prefijo '+'")
    void getUnreadMessages_whenPhoneMissingPlus_retriesWithPlusPrefix() {
        patient.setPhone("+573107984713");
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc())
                .thenReturn(List.of(messageFrom("573107984713")));
        when(patientRepository.findByPhone("573107984713")).thenReturn(Optional.empty());
        when(patientRepository.findByPhone("+573107984713")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION)).thenReturn(List.of());

        List<WhatsappMessageLogDTO> result = whatsappLogService.getUnreadMessages();

        assertThat(result.get(0).getPatientName()).isEqualTo("Pedro Contreras");
        verify(patientRepository).findByPhone("573107984713");
        verify(patientRepository).findByPhone("+573107984713");
    }

    @Test
    @DisplayName("getUnreadMessages: paciente sin citas pendientes muestra 'Sin citas pendientes'")
    void getUnreadMessages_withPatientWithoutPendingAppointments_returnsNoPendingLabel() {
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc())
                .thenReturn(List.of(messageFrom("573107984713")));
        when(patientRepository.findByPhone("573107984713")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION))
                .thenReturn(List.of(pendingAppointment(4L, 99L, 4L)));

        List<WhatsappMessageLogDTO> result = whatsappLogService.getUnreadMessages();

        assertThat(result.get(0).getPatientName()).isEqualTo("Pedro Contreras");
        assertThat(result.get(0).getSpecialty()).isEqualTo("Sin citas pendientes");
        verifyNoInteractions(doctorRepository);
    }

    @Test
    @DisplayName("getUnreadMessages: número desconocido retorna 'Desconocido' / 'No detectada' sin consultar citas")
    void getUnreadMessages_withUnknownPhone_returnsDefaultLabels() {
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc())
                .thenReturn(List.of(messageFrom("573000000000")));
        when(patientRepository.findByPhone("573000000000")).thenReturn(Optional.empty());
        when(patientRepository.findByPhone("+573000000000")).thenReturn(Optional.empty());

        List<WhatsappMessageLogDTO> result = whatsappLogService.getUnreadMessages();

        assertThat(result.get(0).getPatientName()).isEqualTo("Desconocido");
        assertThat(result.get(0).getSpecialty()).isEqualTo("No detectada");
        verifyNoInteractions(appointmentRepository, doctorRepository);
    }

    @Test
    @DisplayName("getUnreadMessages: si el número ya trae '+' y no hay coincidencia, no reintenta la búsqueda")
    void getUnreadMessages_whenPhoneAlreadyHasPlus_doesNotRetry() {
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc())
                .thenReturn(List.of(messageFrom("+573000000000")));
        when(patientRepository.findByPhone("+573000000000")).thenReturn(Optional.empty());

        whatsappLogService.getUnreadMessages();

        verify(patientRepository, times(1)).findByPhone(any());
    }

    @Test
    @DisplayName("getUnreadMessages: médico de la cita no encontrado muestra la especialidad 'Unknown'")
    void getUnreadMessages_whenDoctorNotFound_usesUnknownSpecialty() {
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc())
                .thenReturn(List.of(messageFrom("573107984713")));
        when(patientRepository.findByPhone("573107984713")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByStatus(AppointmentStatus.PENDING_CONFIRMATION))
                .thenReturn(List.of(pendingAppointment(1L, 17L, 404L)));
        when(doctorRepository.findById(404L)).thenReturn(Optional.empty());

        List<WhatsappMessageLogDTO> result = whatsappLogService.getUnreadMessages();

        assertThat(result.get(0).getSpecialty()).isEqualTo("Unknown");
    }

    @Test
    @DisplayName("getUnreadMessages: sin mensajes sin leer retorna una lista vacía")
    void getUnreadMessages_withNoMessages_returnsEmptyList() {
        when(messageLogRepository.findByIsReadFalseOrderByReceivedAtDesc()).thenReturn(List.of());

        List<WhatsappMessageLogDTO> result = whatsappLogService.getUnreadMessages();

        assertThat(result).isEmpty();
        verifyNoInteractions(patientRepository, appointmentRepository, doctorRepository);
    }

    // ------------------------------------------------------------------
    // markAsRead
    // ------------------------------------------------------------------

    @Test
    @DisplayName("markAsRead: marca el mensaje como leído y lo guarda")
    void markAsRead_withExistingMessage_setsReadAndSaves() {
        WhatsappMessageLog log = messageFrom("573107984713");
        when(messageLogRepository.findById(1L)).thenReturn(Optional.of(log));

        whatsappLogService.markAsRead(1L);

        ArgumentCaptor<WhatsappMessageLog> captor = ArgumentCaptor.forClass(WhatsappMessageLog.class);
        verify(messageLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIsRead()).isTrue();
    }

    @Test
    @DisplayName("markAsRead: mensaje inexistente lanza ResourceNotFoundException y no guarda")
    void markAsRead_withUnknownMessage_throwsResourceNotFound() {
        when(messageLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> whatsappLogService.markAsRead(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(messageLogRepository, never()).save(any());
    }
}
