package com.sena.backend.domain.receptionist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceptionistResponse {
    private Long id;
    private String username;
    private String identityDocument;
    private String fullName;
    private String phone;
    private boolean isActive;
}