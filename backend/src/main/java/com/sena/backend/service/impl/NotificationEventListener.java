package com.sena.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.backend.ConsultationScheduledEvent;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Notification;
import com.sena.backend.entity.MedicalRecord;
import com.sena.backend.entity.Patient;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    public NotificationEventListener(ConsultationRepository consultationRepository,
                                     NotificationRepository notificationRepository) {
        this.consultationRepository = consultationRepository;
        this.notificationRepository = notificationRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConsultationScheduled(ConsultationScheduledEvent event) {
        Long consultationId = event.getConsultationId();
        Optional<Consultation> oc = consultationRepository.findById(consultationId);
        if (oc.isEmpty()) return;

        Consultation consultation = oc.get();
        MedicalRecord mr = consultation.getMedicalRecord();
        if (mr == null || mr.getPatient() == null) return;
        Patient patient = mr.getPatient();
        String phone = patient.getPhone();
        if (phone == null || phone.isBlank()) return;

        Notification notif = new Notification();
        notif.setDestinationNumber(phone);
        notif.setStatus("PENDING");
        notif.setProviderId(null);
        notif.setUpdatedAt(OffsetDateTime.now());
        notif.setConsultation(consultation);

        Notification saved = notificationRepository.save(notif);

        Map<String, Object> payload = new HashMap<>();
        payload.put("notification_id", saved.getId());
        payload.put("patient_name", patient.getFullName());
        payload.put("destination_number", saved.getDestinationNumber());
        String doctorName = consultation.getDoctor() != null ? consultation.getDoctor().getFullName() : null;
        payload.put("doctor_name", doctorName);
        String consultationDateStr = consultation.getConsultationDate() != null ? consultation.getConsultationDate().toString() : null;
        payload.put("consultation_date", consultationDateStr);

        try {
            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = objectMapper.writeValueAsString(payload);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            rt.postForEntity(n8nWebhookUrl, entity, String.class);
        } catch (Exception ex) {
            // mark notification as FAILED for consistency
            try {
                saved.setStatus("FAILED");
                notificationRepository.save(saved);
            } catch (Exception saveEx) {
                // If saving the failure state fails, log the error (avoid throwing to not break the async flow)
                saveEx.printStackTrace();
            }
        }
    }
}
