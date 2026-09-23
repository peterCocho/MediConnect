package com.sena.backend.controller;

import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.domain.receptionist.ReceptionistResponse;
import com.sena.backend.domain.receptionist.UpdateReceptionistRequest;
import com.sena.backend.service.ReceptionistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receptionists")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Receptionists", description = "Endpoints for managing receptionist profiles and accounts")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @Operation(summary = "Create a new receptionist", description = "Registers a new receptionist in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receptionist successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @PostMapping
    public ResponseEntity<Void> createReceptionist(@Valid @RequestBody CreateReceptionistRequest req) {
        receptionistService.createReceptionist(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get receptionist by ID", description = "Retrieves the details of a specific receptionist. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receptionist successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Receptionist not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReceptionistResponse> getReceptionistById(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.getReceptionistById(id));
    }

    @Operation(summary = "Get all receptionists", description = "Retrieves a paginated list of all receptionists. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of receptionists successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping
    public ResponseEntity<Page<ReceptionistResponse>> getAllReceptionists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(receptionistService.getAllReceptionists(page, size, sortBy));
    }

    @Operation(summary = "Update a receptionist", description = "Updates an existing receptionist's details. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Receptionist successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Receptionist not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateReceptionist(@PathVariable Long id, @Valid @RequestBody UpdateReceptionistRequest req) {
        receptionistService.updateReceptionist(id, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disable a receptionist", description = "Soft deletes (disables) a receptionist record. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Receptionist successfully disabled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions"),
            @ApiResponse(responseCode = "404", description = "Receptionist not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableReceptionist(@PathVariable Long id) {
        receptionistService.toggleReceptionistStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}