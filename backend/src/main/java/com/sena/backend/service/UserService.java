package com.sena.backend.service;

import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.receptionist.CreateReceptionistRequest;

public interface UserService {
    void assignRoleToUser(String username, String roleName);
    void createDoctor(CreateDoctorRequest req);
    void createReceptionist(CreateReceptionistRequest req);
}
