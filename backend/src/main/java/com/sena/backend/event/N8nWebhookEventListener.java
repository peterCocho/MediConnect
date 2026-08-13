package com.sena.backend.event;

import com.sena.backend.domain.integration.AppointmentConfirmationWebhookDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Patient;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class N8nWebhookEventListener {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    // Executes only after the database transaction commits successfully
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentRequiresConfirmation(AppointmentRequiresConfirmationEvent event) {
        Appointment appointment = appointmentRepository.findById(event.getAppointmentId()).orElse(null);
        if (appointment == null) {
            return;
        }

        Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
        if (patient == null) {
            return;
        }

        // Build the payload for n8n
        AppointmentConfirmationWebhookDTO payload = AppointmentConfirmationWebhookDTO.builder()
                .appointmentId(appointment.getId())
                .patientPhone(patient.getPhone())
                .patientName(patient.getFullName())
                .appointmentDate(appointment.getStartTime())
                .build();

        try {
            // Dispatch the POST request to n8n webhook
            restTemplate.postForEntity(n8nWebhookUrl, payload, Void.class);
        } catch (Exception e) {
            // Log the error.
            // In a production environment, implement a retry mechanism or DLQ (Dead Letter Queue)
            System.err.println("Fallo al enviar el webhook de confirmación a n8n: " + e.getMessage());
        }
    }
}