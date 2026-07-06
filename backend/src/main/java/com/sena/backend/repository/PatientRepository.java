package com.sena.backend.repository;

import com.sena.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByIdentityDocument(String identityDocument);


}
