package com.sena.backend.repository;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Busca las citas de un médico específico dentro de un rango de tiempo.
     * Útil para validaciones de disponibilidad en la capa de servicio.
     */
    List<Appointment> findByDoctorIdAndStartTimeBetween(Long doctorId, OffsetDateTime from, OffsetDateTime to);

    // Fetch paginated appointments within a specific start time range
    Page<Appointment> findByStartTimeBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);

    // NUEVO: Reemplaza a existsByPatientIdAndStatus para soportar múltiples estados a la vez
    boolean existsByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);

    // Fetch all appointments for a specific doctor
    List<Appointment> findByDoctorIdOrderByStartTimeDesc(Long doctorId);

    // Fetch paginated appointments for a specific patient
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    // Fetch paginated appointments for a specific doctor
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    // ACTUALIZADO: Compara contra la constante del Enum directamente en HQL
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
            "WHERE a.doctorId = :doctorId " +
            "AND a.startTime < :endTime " +
            "AND a.endTime > :startTime " +
            "AND a.status != com.sena.backend.domain.AppointmentStatus.CANCELED")
    boolean hasOverlappingAppointments(@Param("doctorId") Long doctorId,
                                       @Param("startTime") OffsetDateTime startTime,
                                       @Param("endTime") OffsetDateTime endTime);

    // ACTUALIZADO: El parámetro de estado ahora es un AppointmentStatus
    List<Appointment> findByStatusAndStartTimeBetween(AppointmentStatus status, OffsetDateTime start, OffsetDateTime end);
}