package com.sena.backend.scheduler;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.service.AppointmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class AppointmentScheduler {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;

    public AppointmentScheduler(AppointmentService appointmentService, AppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(fixedRate = 3600000)
    public void autoCancelUnconfirmedAppointments() {
        // Threshold: Appointments starting in less than 24 hours from exactly now
        OffsetDateTime deadline = OffsetDateTime.now().plusHours(24);

        List<Appointment> abandonedAppointments = appointmentRepository
                .findByStatusAndStartTimeBefore(AppointmentStatus.PENDING_CONFIRMATION, deadline);

        for (Appointment app : abandonedAppointments) {
            try {
                // Execute cancellation through the service to trigger events
                appointmentService.cancelAppointment(
                        app.getId(),
                        "Cancelación automática: Sin confirmación previa al límite de 24h"
                );
            } catch (Exception e) {
                // Log the error to prevent a single corrupt record from breaking the scheduled loop
                System.err.println("Error automatically canceling appointment ID " + app.getId() + ": " + e.getMessage());
            }
        }
    }
}