package com.sena.backend.controller;

import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.domain.receptionist.ReceptionistResponse;
import com.sena.backend.domain.receptionist.UpdateReceptionistRequest;
import com.sena.backend.service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receptionists")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @PostMapping
    public ResponseEntity<Void> createReceptionist(@Valid @RequestBody CreateReceptionistRequest req) {
        receptionistService.createReceptionist(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceptionistResponse> getReceptionistById(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.getReceptionistById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ReceptionistResponse>> getAllReceptionists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(receptionistService.getAllReceptionists(page, size, sortBy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateReceptionist(@PathVariable Long id, @Valid @RequestBody UpdateReceptionistRequest req) {
        receptionistService.updateReceptionist(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableReceptionist(@PathVariable Long id) {
        receptionistService.toggleReceptionistStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}