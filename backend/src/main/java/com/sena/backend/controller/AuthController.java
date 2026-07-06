package com.sena.backend.controller;

import com.sena.backend.domain.AuthResponse;
import com.sena.backend.domain.LoginRequest;
import com.sena.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) throws Exception {
        AuthResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }
}
