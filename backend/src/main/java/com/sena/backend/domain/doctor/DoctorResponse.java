package com.sena.backend.domain.doctor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorResponse {
    private Long id;
    private String documentNumber;
    private String fullName;
    private String email;
    private String phone;
    private String specialty;
    private boolean isActive;
    private String username;
}