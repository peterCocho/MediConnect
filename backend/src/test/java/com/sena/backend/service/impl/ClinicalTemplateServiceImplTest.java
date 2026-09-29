package com.sena.backend.service.impl;

import com.sena.backend.domain.template.ClinicalTemplateRequestDTO;
import com.sena.backend.domain.template.ClinicalTemplateResponseDTO;
import com.sena.backend.entity.ClinicalTemplate;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ClinicalTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ClinicalTemplateServiceImpl.
 *
 * Cubre las plantillas de historia clínica que usan los médicos: creación
 * (nombre único, siempre activa al crearse), listados (todas / solo activas),
 * actualización y activación/desactivación lógica (soft delete).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClinicalTemplateServiceImpl")
class ClinicalTemplateServiceImplTest {

    @Mock
    private ClinicalTemplateRepository repository;

    @InjectMocks
    private ClinicalTemplateServiceImpl service;

    private ClinicalTemplate activeTemplate;
    private ClinicalTemplate inactiveTemplate;

    @BeforeEach
    void setUp() {
        activeTemplate = ClinicalTemplate.builder()
                .id(1L).name("Consulta general").description("Plantilla base")
                .templateContent("Motivo:\nExamen físico:\nPlan:").isActive(true).build();
        inactiveTemplate = ClinicalTemplate.builder()
                .id(2L).name("Control pediátrico").description("Obsoleta")
                .templateContent("Peso:\nTalla:").isActive(false).build();
    }

    private ClinicalTemplateRequestDTO request(String name, String description, String template) {
        ClinicalTemplateRequestDTO dto = new ClinicalTemplateRequestDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setTemplate(template);
        return dto;
    }

    // ------------------------------------------------------------------
    // createTemplate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createTemplate: guarda la plantilla activa con el contenido recibido y retorna el DTO")
    void createTemplate_withValidData_savesActiveTemplateAndReturnsResponse() {
        when(repository.existsByName("Consulta general")).thenReturn(false);
        when(repository.save(any(ClinicalTemplate.class))).thenAnswer(inv -> {
            ClinicalTemplate t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        ClinicalTemplateResponseDTO response =
                service.createTemplate(request("Consulta general", "Plantilla base", "Motivo:\nPlan:"));

        ArgumentCaptor<ClinicalTemplate> captor = ArgumentCaptor.forClass(ClinicalTemplate.class);
        verify(repository).save(captor.capture());
        ClinicalTemplate saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Consulta general");
        assertThat(saved.getDescription()).isEqualTo("Plantilla base");
        assertThat(saved.getTemplateContent()).isEqualTo("Motivo:\nPlan:");
        assertThat(saved.getIsActive()).isTrue();

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Consulta general");
        assertThat(response.getTemplateContent()).isEqualTo("Motivo:\nPlan:");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createTemplate: un nombre ya existente lanza BusinessRuleException (409) y no guarda")
    void createTemplate_withExistingName_throwsBusinessRuleException() {
        when(repository.existsByName("Consulta general")).thenReturn(true);

        assertThatThrownBy(() -> service.createTemplate(request("Consulta general", "x", "Motivo:")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Consulta general");

        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // getAllTemplates / getActiveTemplates
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAllTemplates: retorna todas las plantillas, activas e inactivas, mapeadas a DTO")
    void getAllTemplates_returnsAllMapped() {
        when(repository.findAll()).thenReturn(List.of(activeTemplate, inactiveTemplate));

        List<ClinicalTemplateResponseDTO> result = service.getAllTemplates();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClinicalTemplateResponseDTO::getName)
                .containsExactly("Consulta general", "Control pediátrico");
        assertThat(result).extracting(ClinicalTemplateResponseDTO::getIsActive)
                .containsExactly(true, false);
    }

    @Test
    @DisplayName("getActiveTemplates: consulta solo las plantillas activas en el repositorio")
    void getActiveTemplates_returnsOnlyActiveFromRepository() {
        when(repository.findAllByIsActiveTrue()).thenReturn(List.of(activeTemplate));

        List<ClinicalTemplateResponseDTO> result = service.getActiveTemplates();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Consulta general");
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("getActiveTemplates: sin plantillas activas retorna una lista vacía")
    void getActiveTemplates_withNoActiveTemplates_returnsEmptyList() {
        when(repository.findAllByIsActiveTrue()).thenReturn(List.of());

        assertThat(service.getActiveTemplates()).isEmpty();
    }

    // ------------------------------------------------------------------
    // updateTemplate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateTemplate: actualiza nombre, descripción y contenido sin cambiar el estado activo")
    void updateTemplate_withValidData_updatesFieldsAndKeepsActiveFlag() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
        when(repository.save(any(ClinicalTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        ClinicalTemplateResponseDTO response =
                service.updateTemplate(1L, request("Consulta general v2", "Nueva descripción", "Motivo:\nDiagnóstico:"));

        ArgumentCaptor<ClinicalTemplate> captor = ArgumentCaptor.forClass(ClinicalTemplate.class);
        verify(repository).save(captor.capture());
        ClinicalTemplate saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Consulta general v2");
        assertThat(saved.getDescription()).isEqualTo("Nueva descripción");
        assertThat(saved.getTemplateContent()).isEqualTo("Motivo:\nDiagnóstico:");
        assertThat(saved.getIsActive()).isTrue();

        assertThat(response.getName()).isEqualTo("Consulta general v2");
        assertThat(response.getTemplateContent()).isEqualTo("Motivo:\nDiagnóstico:");
    }

    @Test
    @DisplayName("updateTemplate: renombrar a un nombre que ya usa otra plantilla lanza BusinessRuleException y no guarda")
    void updateTemplate_withNameTakenByAnotherTemplate_throwsBusinessRuleException() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
        when(repository.existsByName("Control pediátrico")).thenReturn(true);

        assertThatThrownBy(() -> service.updateTemplate(1L, request("Control pediátrico", "x", "y")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Control pediátrico");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateTemplate: conservar el mismo nombre no valida duplicados contra sí misma")
    void updateTemplate_withSameName_doesNotCheckDuplicateAgainstItself() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
        when(repository.save(any(ClinicalTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateTemplate(1L, request("Consulta general", "Descripción nueva", "Motivo:"));

        verify(repository, never()).existsByName(any());
        verify(repository).save(any(ClinicalTemplate.class));
    }

    @Test
    @DisplayName("updateTemplate: plantilla inexistente lanza ResourceNotFoundException y no guarda")
    void updateTemplate_withUnknownId_throwsResourceNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTemplate(999L, request("x", "y", "z")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // deactivateTemplate / activateTemplate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("deactivateTemplate: desactiva la plantilla (borrado lógico) y la guarda")
    void deactivateTemplate_withExistingId_setsInactiveAndSaves() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));

        service.deactivateTemplate(1L);

        ArgumentCaptor<ClinicalTemplate> captor = ArgumentCaptor.forClass(ClinicalTemplate.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isFalse();
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deactivateTemplate: plantilla inexistente lanza ResourceNotFoundException y no guarda")
    void deactivateTemplate_withUnknownId_throwsResourceNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivateTemplate(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("activateTemplate: reactiva la plantilla y retorna el DTO con isActive = true")
    void activateTemplate_withExistingId_setsActiveAndReturnsResponse() {
        when(repository.findById(2L)).thenReturn(Optional.of(inactiveTemplate));
        when(repository.save(any(ClinicalTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        ClinicalTemplateResponseDTO response = service.activateTemplate(2L);

        ArgumentCaptor<ClinicalTemplate> captor = ArgumentCaptor.forClass(ClinicalTemplate.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isTrue();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("activateTemplate: plantilla inexistente lanza ResourceNotFoundException y no guarda")
    void activateTemplate_withUnknownId_throwsResourceNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateTemplate(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
    }
}