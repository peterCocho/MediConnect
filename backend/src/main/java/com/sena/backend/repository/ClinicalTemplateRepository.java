package com.sena.backend.repository;

import com.sena.backend.entity.ClinicalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalTemplateRepository extends JpaRepository<ClinicalTemplate, Long> {

    // Fetch only active templates for doctors
    List<ClinicalTemplate> findAllByIsActiveTrue();

    // Check for uniqueness before insertion
    boolean existsByName(String name);
}