package com.sena.backend.domain.report;

// Interface-based projection to capture the GROUP BY results directly from PostgreSQL
public interface DiagnosisCountProjection {
    String getIcd10Code();
    Long getCount();
}