package com.sena.backend.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload for an ADMIN to create internal staff accounts.
 * The role is explicit and constrained to operational staff roles only;
 * ROLE_ADMIN cannot be provisioned through this endpoint.
 */
@Data
public class CreateStaffRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    @Pattern(regexp = "ROLE_DOCTOR|ROLE_RECEPTION",
            message = "roleName must be ROLE_DOCTOR or ROLE_RECEPTION")
    private String roleName;
}
