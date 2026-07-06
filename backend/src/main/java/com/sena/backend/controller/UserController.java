package com.sena.backend.controller;

import java.util.List;

import com.sena.backend.domain.doctor.DoctorResponse;
import com.sena.backend.domain.doctor.UpdateDoctorRequest;
import com.sena.backend.domain.role.AssignRoleRequest;
import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

    @GetMapping("/doctors/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getDoctorById(id));
    }

    @PutMapping("/doctors/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updateDoctor(@PathVariable Long id, @Valid @RequestBody UpdateDoctorRequest req) {
        userService.updateDoctor(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/doctors/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateDoctor(@PathVariable Long id) {
        userService.toggleDoctorStatus(id, false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/doctors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) Boolean isActive
    ) {
        return ResponseEntity.ok(userService.getAllDoctors(page, size, sortBy, fullName, specialty, isActive));
    }

}