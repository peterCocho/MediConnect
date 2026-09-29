package com.sena.backend.controller;

import com.sena.backend.domain.doctor.DoctorResponse;
import com.sena.backend.domain.doctor.UpdateDoctorRequest;
import com.sena.backend.domain.role.AssignRoleRequest;
import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing system users, doctors, and role assignments")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new doctor", description = "Registers a new doctor and creates their user account. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @PostMapping("/doctors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> createDoctor(@Valid @RequestBody CreateDoctorRequest req) {
        userService.createDoctor(req);
        return ResponseEntity.status(201).build();
    }

    // La creación de recepcionistas vive en ReceptionistController (POST /api/receptionists),
    // que sí persiste el perfil completo (documento, nombre, teléfono). El endpoint que
    // existía aquí (POST /api/users/receptionists) solo creaba la cuenta de usuario y
    // descartaba esos datos, dejando registros huérfanos. Se elimina para evitar esa ruta.

    @Operation(summary = "Assign a role", description = "Assigns a specific role to an existing user. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role successfully assigned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "User or role not found")
    })
    @PostMapping("/assign-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRole(@Valid @RequestBody AssignRoleRequest req) {
        userService.assignRoleToUser(req.getUsername(), req.getRoleName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get doctor by ID", description = "Retrieves the details of a specific doctor. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/doctors/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTION')")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getDoctorById(id));
    }

    @Operation(summary = "Update a doctor", description = "Updates an existing doctor's details. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PutMapping("/doctors/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updateDoctor(@PathVariable Long id, @Valid @RequestBody UpdateDoctorRequest req) {
        userService.updateDoctor(id, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate a doctor", description = "Soft deletes (deactivates) a doctor's profile. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor successfully deactivated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @DeleteMapping("/doctors/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateDoctor(@PathVariable Long id) {
        userService.toggleDoctorStatus(id, false);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all doctors", description = "Retrieves a paginated list of doctors, optionally filtered by name, specialty, or active status. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of doctors successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTION')")
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