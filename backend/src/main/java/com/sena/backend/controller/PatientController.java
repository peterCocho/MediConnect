package com.sena.backend.controller;

import com.sena.backend.domain.patient.CreatePatientRequest;
import com.sena.backend.domain.patient.PatientResponse;
import com.sena.backend.domain.patient.UpdatePatientRequest;
import com.sena.backend.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Void> createPatient(@RequestBody CreatePatientRequest req) {
        patientService.createPatient(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(patientService.getAllPatients(page, size, sortBy));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Void> updatePatient(@PathVariable Long id, @RequestBody UpdatePatientRequest req) {
        patientService.updatePatient(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RECEPTION')")
    public ResponseEntity<Void> disablePatient(@PathVariable Long id) {
        patientService.togglePatientStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}