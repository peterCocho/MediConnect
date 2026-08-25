package com.sena.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.event.AppointmentCanceledEvent;
import com.sena.backend.entity.*;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.NotificationRepository;
import com.sena.backend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    public NotificationEventListener(ConsultationRepository consultationRepository,
                                     NotificationRepository notificationRepository,
                                     AppointmentRepository appointmentRepository,
                                     PatientRepository patientRepository,
                                     RestTemplate restTemplate) {
        this.consultationRepository = consultationRepository;
        this.notificationRepository = notificationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.restTemplate = restTemplate;
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConsultationScheduled(ConsultationScheduledEvent event) {
        Long consultationId = event.getConsultationId();
        Optional<Consultation> oc = consultationRepository.findById(consultationId);
        if (oc.isEmpty()) return;

        Consultation consultation = oc.get();

        // BUSINESS RULE: Avoid spam.
        // Only send immediate confirmation if appointment is more than 48 hours away.
        // Otherwise, the 24h scheduler will handle it.
        OffsetDateTime limit = OffsetDateTime.now().plusDays(2);
        if (consultation.getConsultationDate() != null && consultation.getConsultationDate().isBefore(limit)) {
            return; // Do not send immediate confirmation.
        }

        sendNotification(consultation, "CONFIRMATION");
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentCanceled(AppointmentCanceledEvent event) {
        Long appointmentId = event.getAppointmentId();
        Optional<Appointment> oa = appointmentRepository.findById(appointmentId);
        if (oa.isEmpty()) return;

        Appointment appointment = oa.get();
        if (appointment.getConsultation() == null) return;

        // Send cancellation notification based on linked consultation
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
        // Status changed to LOGGED to reflect internal audit without external dispatch
        notif.setStatus("LOGGED");
        notif.setType(notificationType);
        notif.setUpdatedAt(OffsetDateTime.now());
        notif.setConsultation(consultation);

        // Save the notification in the database for local audit trail
        notificationRepository.save(notif);

        // External HTTP request logic removed to prevent payload collisions
        // with the Outbound Confirmation n8n webhook.
    }
}