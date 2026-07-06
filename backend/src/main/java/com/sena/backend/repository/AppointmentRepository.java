package com.sena.backend.repository;

import com.sena.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByDoctorIdAndStartTimeBetween(UUID doctorId, OffsetDateTime from, OffsetDateTime to);
}
