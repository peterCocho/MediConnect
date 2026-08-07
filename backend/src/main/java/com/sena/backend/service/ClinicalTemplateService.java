package com.sena.backend.service;

import com.sena.backend.domain.template.ClinicalTemplateRequestDTO;
import com.sena.backend.domain.template.ClinicalTemplateResponseDTO;
import java.util.List;

public interface ClinicalTemplateService {
    ClinicalTemplateResponseDTO createTemplate(ClinicalTemplateRequestDTO request);
    List<ClinicalTemplateResponseDTO> getAllTemplates();
    List<ClinicalTemplateResponseDTO> getActiveTemplates();
    void deactivateTemplate(Long id);
}