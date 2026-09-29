package com.sena.backend.service.impl;

import com.sena.backend.domain.template.ClinicalTemplateRequestDTO;
import com.sena.backend.domain.template.ClinicalTemplateResponseDTO;
import com.sena.backend.entity.ClinicalTemplate;
import com.sena.backend.exception.BusinessRuleException;
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
            // BusinessRuleException -> the GlobalExceptionHandler maps it to 409 CONFLICT.
            throw new BusinessRuleException("Ya existe una plantilla con el nombre: " + request.getName());
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
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));

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

    @Override
    @Transactional
    public ClinicalTemplateResponseDTO updateTemplate(Long id, ClinicalTemplateRequestDTO request) {
        ClinicalTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));

        // If the new name belongs to another template, we prevent the unique
        // database constraint from being the only line of defense (that would end in a 500).
        if (!template.getName().equals(request.getName()) && repository.existsByName(request.getName())) {
            throw new BusinessRuleException("Ya existe una plantilla con el nombre: " + request.getName());
        }

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setTemplateContent(request.getTemplate());

        ClinicalTemplate updated = repository.save(template);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ClinicalTemplateResponseDTO activateTemplate(Long id) {
        ClinicalTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));
        template.setIsActive(true);
        ClinicalTemplate updated = repository.save(template);
        return mapToResponse(updated);
    }
}