package com.sena.backend.controller;

import com.sena.backend.domain.role.AssignRoleRequest;
import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/doctors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> createDoctor(@Valid @RequestBody CreateDoctorRequest req) {
        userService.createDoctor(req);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/receptionists")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> createReceptionist(@Valid @RequestBody CreateReceptionistRequest req) {
        userService.createReceptionist(req);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/assign-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRole(@Valid @RequestBody AssignRoleRequest req) {
        userService.assignRoleToUser(req.getUsername(), req.getRoleName());
        return ResponseEntity.noContent().build();
    }
}