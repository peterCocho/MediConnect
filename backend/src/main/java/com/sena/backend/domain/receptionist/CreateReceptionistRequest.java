package com.sena.backend.domain.receptionist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReceptionistRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank
    private String identityDocument;

    @NotBlank
    private String phone;
}