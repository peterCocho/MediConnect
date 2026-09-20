package com.sena.backend.service;

import com.sena.backend.domain.template.ClinicalTemplateRequestDTO;
import com.sena.backend.domain.template.ClinicalTemplateResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ClinicalTemplateService {
    ClinicalTemplateResponseDTO createTemplate(ClinicalTemplateRequestDTO request);
    List<ClinicalTemplateResponseDTO> getAllTemplates();
    List<ClinicalTemplateResponseDTO> getActiveTemplates();
    void deactivateTemplate(Long id);

    ClinicalTemplateResponseDTO updateTemplate(Long id, @Valid ClinicalTemplateRequestDTO request);

    ClinicalTemplateResponseDTO activateTemplate(Long id);
}