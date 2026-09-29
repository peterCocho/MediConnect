package com.sena.backend.service;

import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.doctor.DoctorResponse;
import com.sena.backend.domain.doctor.UpdateDoctorRequest;
import org.springframework.data.domain.Page;

public interface UserService {
    void assignRoleToUser(String username, String roleName);
    void createDoctor(CreateDoctorRequest req);

    Page<DoctorResponse> getAllDoctors(int page, int size, String sortBy, String fullName, String specialty, Boolean isActive);

    DoctorResponse getDoctorById(Long id);
    void updateDoctor(Long id, UpdateDoctorRequest req);
    void toggleDoctorStatus(Long id, boolean status);
}
