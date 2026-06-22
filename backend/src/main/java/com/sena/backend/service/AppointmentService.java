package com.sena.backend.service;

import com.sena.backend.entity.Appointment;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AppointmentService {
    Appointment bookAppointment(UUID patientId, UUID doctorId, OffsetDateTime start, OffsetDateTime end) throws Exception;
}
