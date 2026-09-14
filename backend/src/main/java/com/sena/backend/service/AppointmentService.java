package com.sena.backend.service;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(@NotNull Long patientId, @NotNull Long doctorId, OffsetDateTime start, OffsetDateTime end) throws Exception;

    @Transactional
    Appointment cancelAppointment(Long appointmentId, String reason);

    Appointment getAppointmentById(Long id);

    Page<Appointment> getPatientAppointments(Long patientId, Pageable pageable);
    
    Page<Appointment> getDoctorAppointments(Long doctorId, Pageable pageable);

    Page<Appointment> getAllAppointments(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

    Appointment confirmAppointment(Long appointmentId);

    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);
}
