package com.sena.backend.repository;

import com.sena.backend.domain.AppointmentStatus;
import com.sena.backend.domain.report.DiagnosisCountProjection;
import com.sena.backend.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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

    Page<Consultation> findByDoctorId(Long doctorId, Pageable pageable);

    Optional<Consultation> findByAppointmentId(Long appointmentId);

    List<Consultation> findByMedicalRecordIdAndStatusOrderByConsultationDateDesc(Long medicalRecordId, AppointmentStatus status);

    // Count volume of consultations within a date range
    long countByStatusAndConsultationDateBetween(AppointmentStatus status, OffsetDateTime startDate, OffsetDateTime endDate);

    // Analytical grouping query for ICD-10 prevalence excluding null or empty codes
    @Query("SELECT c.icd10Code AS icd10Code, COUNT(c) AS count " +
            "FROM Consultation c " +
            "WHERE c.status = :status " +
            "AND c.consultationDate >= :startDate " +
            "AND c.consultationDate <= :endDate " +
            "AND c.icd10Code IS NOT NULL " +
            "AND TRIM(c.icd10Code) != '' " +
            "GROUP BY c.icd10Code " +
            "ORDER BY count DESC")
    List<DiagnosisCountProjection> findTopDiagnosesByDateRange(
            @Param("status") AppointmentStatus status,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);
}