package com.sena.backend.controller;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import com.sena.backend.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Endpoints for managing patient records")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "Create a new patient", description = "Registers a new patient in the system. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Void> createPatient(@RequestBody CreatePatientRequest req) {
        patientService.createPatient(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get patient by ID", description = "Retrieves the details of a specific patient. Accessible by RECEPTIONIST and DOCTOR roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_DOCTOR')")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @Operation(summary = "Get all patients", description = "Retrieves a paginated list of all patients. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of patients successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(patientService.getAllPatients(page, size, sortBy));
    }

    @Operation(summary = "Update a patient", description = "Updates an existing patient's details. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Void> updatePatient(@PathVariable Long id, @RequestBody UpdatePatientRequest req) {
        patientService.updatePatient(id, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disable a patient", description = "Soft deletes (disables) a patient record. Requires RECEPTION role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient successfully disabled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Void> disablePatient(@PathVariable Long id) {
        patientService.togglePatientStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}