package com.sena.backend.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Consultation;
import com.sena.backend.entity.Notification;
import com.sena.backend.entity.Patient;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.ConsultationRepository;
import com.sena.backend.repository.NotificationRepository;
import com.sena.backend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${n8n.webhook.reminder-url}") // Se Necesita una URL diferente en n8n para recordatorios
    private String n8nReminderWebhookUrl;
    private ConsultationRepository consultationRepository;

    public NotificationScheduler(AppointmentRepository appointmentRepository,
                                 NotificationRepository notificationRepository,
                                 PatientRepository patientRepository,
                                 RestTemplate restTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.patientRepository = patientRepository;
        this.restTemplate = restTemplate;
    }

    // Se ejecuta cada hora (cron: 0 0 * * * *)
    @Scheduled(cron = "0 0 * * * *")
    public void send24hReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime tomorrowStart = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime tomorrowEnd = tomorrowStart.plusDays(1);

        // Busca citas agendadas ('BOOKED') para mañana
        // Busca citas confirmadas ('SCHEDULED') para mañana
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByStatusAndStartTimeBetween(AppointmentStatus.SCHEDULED, tomorrowStart, tomorrowEnd);

        for (Appointment app : upcomingAppointments) {
            // Lógica para evitar reenvíos si ya existe una notificación de recordatorio para esta consulta
            // Asumimos que app.getConsultation() no es nulo por diseño

            // ... (implementar búsqueda de notificaciones existentes si se desea)

            // Simplemente enviamos el recordatorio. N8n debería manejar la plantilla de "Recordatorio"
            // basándose en el payload.

            triggerNotification(app, "REMINDER_24H");
        }
    }

    private void triggerNotification(Appointment appointment, String notificationType) {
        // 1. Fetch related Consultation
        Consultation consultation = consultationRepository.findByAppointmentId(appointment.getId()).orElse(null);
        if (consultation == null) {
            return;
        }

        // 2. Idempotency check: prevent duplicate reminders for the same consultation
        boolean alreadyTriggered = notificationRepository.existsByConsultationIdAndType(consultation.getId(), notificationType);
        if (alreadyTriggered) {
            return;
        }

        Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
        if (patient == null) {
            return;
        }

        // 3. Register the notification intent in the database with required fields
        Notification notification = new Notification();
        notification.setConsultation(consultation);
        notification.setType(notificationType);
        notification.setDestinationNumber(patient.getPhone());
        notification.setStatus("PENDING");
        notification.setUpdatedAt(OffsetDateTime.now());

        notification = notificationRepository.save(notification);

        // 4. Prepare the payload for n8n
        Map<String, Object> payload = new HashMap<>();
        payload.put("appointmentId", appointment.getId());
        payload.put("patientPhone", patient.getPhone());
        payload.put("patientName", patient.getFullName());
        payload.put("appointmentDate", appointment.getStartTime().toString());
        payload.put("notificationType", notificationType);

        // 5. Dispatch the HTTP request and update the final status
        try {
            restTemplate.postForEntity(n8nReminderWebhookUrl, payload, Void.class);
            notification.setStatus("SENT");
        } catch (Exception e) {
            notification.setStatus("FAILED");
            System.err.println("Fallo al enviar el recordatorio a n8n para la cita " + appointment.getId() + ": " + e.getMessage());
        } finally {
            notification.setUpdatedAt(OffsetDateTime.now());
            notificationRepository.save(notification);
        }
    }
}