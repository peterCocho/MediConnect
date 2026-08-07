package com.sena.backend.service.impl;

import com.sena.backend.domain.template.ClinicalTemplateRequestDTO;
import com.sena.backend.domain.template.ClinicalTemplateResponseDTO;
import com.sena.backend.entity.ClinicalTemplate;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ClinicalTemplateRepository;
import com.sena.backend.service.ClinicalTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicalTemplateServiceImpl implements ClinicalTemplateService {

    private final ClinicalTemplateRepository repository;

    @Override
    @Transactional
    public ClinicalTemplateResponseDTO createTemplate(ClinicalTemplateRequestDTO request) {
        if (repository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Template name already exists");
        }

        ClinicalTemplate template = ClinicalTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .templateContent(request.getTemplate())
                .isActive(true)
                .build();

        ClinicalTemplate saved = repository.save(template);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalTemplateResponseDTO> getAllTemplates() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalTemplateResponseDTO> getActiveTemplates() {
        return repository.findAllByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivateTemplate(Long id) {
        ClinicalTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        // Soft delete logic
        template.setIsActive(false);
        repository.save(template);
    }

    private ClinicalTemplateResponseDTO mapToResponse(ClinicalTemplate entity) {
        return ClinicalTemplateResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .templateContent(entity.getTemplateContent())
                .isActive(entity.getIsActive())
                .build();
    }
}