package com.sena.backend.controller;

import com.sena.backend.domain.AuthResponse;
import com.sena.backend.domain.LoginRequest;
import com.sena.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token generation")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User login", description = "Authenticates a user with username and password, returning a JWT token. Publicly accessible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) throws Exception {
        AuthResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }
}