package com.sena.backend.repository;

import com.sena.backend.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /**
     * Pessimistic lock to check overlapping consultations for a doctor within a date/time range.
     * Use this method inside a @Transactional service when attempting to schedule to prevent race conditions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Consultation c WHERE c.doctor.id = :doctorId AND c.consultationDate >= :from AND c.consultationDate <= :to")
    List<Consultation> findOverlappingForDoctorForUpdate(@Param("doctorId") Long doctorId,
                                                         @Param("from") OffsetDateTime from,
                                                         @Param("to") OffsetDateTime to);
}
