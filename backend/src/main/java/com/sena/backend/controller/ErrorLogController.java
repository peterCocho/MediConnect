package com.sena.backend.controller;

import com.sena.backend.entity.ErrorLog;
import com.sena.backend.repository.ErrorLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
@Tag(name = "System Errors", description = "Endpoints for monitoring and managing system error logs")
public class ErrorLogController {

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogController(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    // Endpoint explicitly requested by the frontend to display the error count
    @Operation(summary = "Get total error count", description = "Retrieves the total number of system errors logged. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Error count successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Long>> getErrorCount() {
        long count = errorLogRepository.count();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // Retrieves a paginated and descending ordered list of system errors
    @Operation(summary = "Get error logs", description = "Retrieves a paginated and descending ordered list of system errors. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of error logs successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<ErrorLog>> getErrorDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("timestamp").descending());

        return ResponseEntity.ok(errorLogRepository.findAll(pageable));
    }

    // Deletes a specific error log entry after it has been reviewed
    @Operation(summary = "Dismiss an error log", description = "Deletes a specific error log entry by its ID after it has been reviewed. Accessible by ADMIN and RECEPTIONIST roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Error log successfully dismissed (deleted)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Void> dismissError(@PathVariable Long id) {
        if (errorLogRepository.existsById(id)) {
            errorLogRepository.deleteById(id);
        }
        return ResponseEntity.noContent().build();
    }
}