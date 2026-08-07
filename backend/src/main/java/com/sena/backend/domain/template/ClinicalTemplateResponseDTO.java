package com.sena.backend.domain.template;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClinicalTemplateResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String templateContent;
    private Boolean isActive;
}