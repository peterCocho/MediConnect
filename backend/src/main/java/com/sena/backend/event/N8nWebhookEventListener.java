package com.sena.backend.event;

import com.sena.backend.domain.integration.AppointmentConfirmationWebhookDTO;
import com.sena.backend.entity.Appointment;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Patient;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.PatientRepository;
import com.sena.backend.service.ErrorLogService;
import jakarta.persistence.EntityNotFoundException;
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
    private final ErrorLogService errorLogService; // Injected custom service
    private final DoctorRepository doctorRepository;


    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentRequiresConfirmation(AppointmentRequiresConfirmationEvent event) {
        Appointment appointment = appointmentRepository.findById(event.getAppointmentId()).orElse(null);
        if (appointment == null) {
            return;
        }

// Fetch patient details using patientId
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient no encontrado con ID: " + appointment.getPatientId()));

// Fetch doctor details using doctorId
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor no encontrado con ID: " + appointment.getDoctorId()));

// Build the DTO mapping the specific fields from the fetched entities
        AppointmentConfirmationWebhookDTO payload = AppointmentConfirmationWebhookDTO.builder()
                .appointmentId(appointment.getId())
                .patientPhone(patient.getPhone())
                .patientName(patient.getFullName())
                .appointmentDate(appointment.getStartTime())
                .doctorName(doctor.getFullName()) // Adjust if Doctor uses a linked User entity
                .specialty(doctor.getSpecialty())
                .build();

        try {
            restTemplate.postForEntity(n8nWebhookUrl, payload, Void.class);
        } catch (Exception e) {
            // Save human-readable error if n8n is completely down
            errorLogService.saveIntegrationError(
                    "Confirmación de Citas",
                    patient.getFullName(),
                    patient.getPhone(),
                    "El servidor de n8n está apagado o inaccesible."
            );
        }
    }
}