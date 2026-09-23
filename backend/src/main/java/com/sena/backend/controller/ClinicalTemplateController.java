package com.sena.backend.controller;

import com.sena.backend.domain.template.ClinicalTemplateRequestDTO;
import com.sena.backend.domain.template.ClinicalTemplateResponseDTO;
import com.sena.backend.service.ClinicalTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "Clinical Templates", description = "Endpoints for managing clinical note templates")
public class ClinicalTemplateController {

    private final ClinicalTemplateService templateService;

    @Operation(summary = "Create a new clinical template", description = "Creates a new clinical template for medical consultations. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clinical template successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClinicalTemplateResponseDTO> createTemplate(
            @Valid @RequestBody ClinicalTemplateRequestDTO request) {
        return new ResponseEntity<>(templateService.createTemplate(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all clinical templates", description = "Retrieves a list of all clinical templates, including inactive ones. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of clinical templates successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClinicalTemplateResponseDTO>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @Operation(summary = "Get active clinical templates", description = "Retrieves a list of active clinical templates to be used during consultations. Accessible by ADMIN and DOCTOR roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active clinical templates successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<ClinicalTemplateResponseDTO>> getActiveTemplates() {
        // Doctors need access to active templates during consultations
        return ResponseEntity.ok(templateService.getActiveTemplates());
    }

    @Operation(summary = "Deactivate a clinical template", description = "Sets a specific clinical template as inactive. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Clinical template successfully deactivated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Clinical template not found")
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long id) {
        templateService.deactivateTemplate(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Activate a clinical template", description = "Sets a specific clinical template as active. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Clinical template successfully activated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Clinical template not found")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateTemplate(@PathVariable Long id) {
        templateService.activateTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a clinical template", description = "Updates an existing clinical template's details. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clinical template successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Clinical template not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClinicalTemplateResponseDTO> updateTemplate(@PathVariable Long id,
            @Valid @RequestBody ClinicalTemplateRequestDTO request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }
}