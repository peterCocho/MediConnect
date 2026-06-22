package com.sena.backend.domain.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank
    private String name;
    private String description;
}
