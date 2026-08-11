package com.sena.backend.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Notification;
import com.sena.backend.repository.AppointmentRepository;
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
        // ... Lógica similar a NotificationEventListener para crear la entidad Notification,
        // armar el payload (incluyendo el notificationType) y hacer el POST síncrono al webhook de n8n.
        // Al final, guardar el estado (SENT o FAILED) de la notificación en la DB.
    }
}