package com.sena.backend.repository;

import com.sena.backend.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByRecordNumber(String recordNumber);
    Optional<MedicalRecord> findByPatientId(Long patientId);
}
