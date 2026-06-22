package com.sena.backend.domain.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String roleName; // e.g., ROLE_DOCTOR
}
