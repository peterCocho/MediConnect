package com.sena.backend.domain.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponse {
    private Long id;
    private String identityDocument;
    private String fullName;
    private String phone;
    private LocalDate birthDate;
    private boolean isActive;
}
