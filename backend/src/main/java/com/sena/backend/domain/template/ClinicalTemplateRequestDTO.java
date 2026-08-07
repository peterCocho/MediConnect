package com.sena.backend.domain.template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClinicalTemplateRequestDTO {

    @NotBlank(message = "El nombre de la plantilla es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "La estructura base de la plantilla es obligatoria")
    // Regex denies <, > and & to strictly prevent HTML tags and entities, allowing \n
    @Pattern(regexp = "^[^<>&]*$", message = "El texto contiene caracteres no permitidos. No se admiten etiquetas HTML o scripts.")
    private String template;
}