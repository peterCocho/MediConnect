package com.sena.backend.service.impl;

import com.sena.backend.entity.Appointment;
import com.sena.backend.repository.AppointmentRepository;
import com.sena.backend.service.AppointmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service that demonstrates DB-level concurrency control for booking appointments.
 * Primary strategy: rely on database UNIQUE constraint (doctor_id, start_time) and handle retries.
 * For stricter guarantees use SELECT ... FOR UPDATE on a schedule row (pessimistic lock) or SERIALIZABLE isolation with retry.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional
    public Appointment bookAppointment(UUID patientId, UUID doctorId, OffsetDateTime start, OffsetDateTime end) throws Exception {
        int attempts = 0;
        int maxAttempts = 3;
        while (true) {
            try {
                Appointment ap = new Appointment();
                ap.setPatientId(patientId);
                ap.setDoctorId(doctorId);
                ap.setStartTime(start);
                ap.setEndTime(end);
                ap.setStatus("BOOKED");
                return appointmentRepository.save(ap);
            } catch (DataIntegrityViolationException ex) {
                // Likely unique constraint violation (slot taken). Retry with backoff.
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new Exception("Failed to book appointment after " + attempts + " attempts: slot unavailable", ex);
                }
                try { Thread.sleep(100L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
