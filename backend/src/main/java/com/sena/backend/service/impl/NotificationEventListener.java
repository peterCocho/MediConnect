package com.sena.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.event.AppointmentCanceledEvent; // Nuevo import
import com.sena.backend.entity.*;
import com.sena.backend.repository.AppointmentRepository; // Nuevo import
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.NotificationRepository;
import com.sena.backend.repository.PatientRepository; // Nuevo import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class NotificationEventListener {

    private final ConsultationRepository consultationRepository;
    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository; // Nueva dependencia
    private final PatientRepository patientRepository; // Nueva dependencia
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    public NotificationEventListener(ConsultationRepository consultationRepository,
                                     NotificationRepository notificationRepository,
                                     AppointmentRepository appointmentRepository, // Inyectar
                                     PatientRepository patientRepository, // Inyectar
                                     RestTemplate restTemplate) {
        this.consultationRepository = consultationRepository;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.restTemplate = restTemplate;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConsultationScheduled(ConsultationScheduledEvent event) {
        Long consultationId = event.getConsultationId();
        Optional<Consultation> oc = consultationRepository.findById(consultationId);
        if (oc.isEmpty()) return;

        Consultation consultation = oc.get();

        // REGLA DE NEGOCIO: Evitar spam.
        // Solo enviar confirmación inmediata si la cita es para dentro de más de 48 horas.
        // De lo contrario, el scheduler de 24h se encargará.
        OffsetDateTime limit = OffsetDateTime.now().plusDays(2);
        if (consultation.getConsultationDate() != null && consultation.getConsultationDate().isBefore(limit)) {
            return; // No enviar confirmación inmediata.
        }

        sendNotification(consultation, "CONFIRMATION");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentCanceled(AppointmentCanceledEvent event) {
        Long appointmentId = event.getAppointmentId();
        Optional<Appointment> oa = appointmentRepository.findById(appointmentId);
        if (oa.isEmpty()) return;

        Appointment appointment = oa.get();
        if (appointment.getConsultation() == null) return;

        // Enviamos notificación de cancelación basándonos en la consulta vinculada
        sendNotification(appointment.getConsultation(), "CANCELLATION");
    }

    private void sendNotification(Consultation consultation, String notificationType) {
        MedicalRecord mr = consultation.getMedicalRecord();
        if (mr == null || mr.getPatient() == null) return;
        Patient patient = mr.getPatient();
        String phone = patient.getPhone();
        if (phone == null || phone.isBlank()) return;

        Notification notif = new Notification();
        notif.setDestinationNumber(phone);
        notif.setStatus("PENDING");
        notif.setUpdatedAt(OffsetDateTime.now());
        notif.setConsultation(consultation);

        Notification saved = notificationRepository.save(notif);

        Map<String, Object> payload = new HashMap<>();
        payload.put("notification_id", saved.getId());
        payload.put("notification_type", notificationType); // ¡Importante para n8n! CONFIRMATION, CANCELLATION
        payload.put("patient_name", patient.getFullName());
        payload.put("destination_number", saved.getDestinationNumber());
        payload.put("doctor_name", consultation.getDoctor() != null ? consultation.getDoctor().getFullName() : null);
        payload.put("consultation_date", consultation.getConsultationDate() != null ? consultation.getConsultationDate().toString() : null);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = objectMapper.writeValueAsString(payload);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(n8nWebhookUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                saved.setStatus("SENT");
            } else {
                saved.setStatus("FAILED");
            }
            notificationRepository.save(saved);

        } catch (Exception ex) {
            saved.setStatus("FAILED");
            notificationRepository.save(saved);
            ex.printStackTrace();
        }
    }
}