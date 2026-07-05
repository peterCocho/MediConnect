// 2. src/main/java/com/sena/backend/dto/UpdateDoctorRequest.java
package com.sena.backend.domain.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDoctorRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String specialty;

    @NotNull
    private Boolean isActive;
}