package com.sena.backend.domain.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload for an ADMIN to provision a doctor: creates the user account
 * (ROLE_DOCTOR) and the linked clinical profile in the doctors table.
 */
@Data
public class CreateDoctorRequest {

    // --- Acces Credentials ---
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    // --- Clinical Profile (table doctors) ---
    @NotBlank
    @Size(max = 20)
    private String documentNumber;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 50)
    private String specialty;
}
