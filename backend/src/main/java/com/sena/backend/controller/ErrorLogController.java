package com.sena.backend.controller;

import com.sena.backend.entity.ErrorLog;
import com.sena.backend.repository.ErrorLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorLogController {

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogController(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    // Endpoint explicitly requested by the frontend to display the error count
    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Long>> getErrorCount() {
        long count = errorLogRepository.count();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // Devuelve la lista completa de errores del sistema
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<List<ErrorLog>> getErrorDetails() {
        return ResponseEntity.ok(errorLogRepository.findAll());
    }

    // Deletes a specific error log entry after it has been reviewed
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION', 'ROLE_ADMIN')")
    public ResponseEntity<Void> dismissError(@PathVariable Long id) {
        if (errorLogRepository.existsById(id)) {
            errorLogRepository.deleteById(id);
        }
        return ResponseEntity.noContent().build();
    }
}