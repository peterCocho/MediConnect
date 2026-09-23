package com.sena.backend.controller;

import com.sena.backend.domain.role.CreateRoleRequest;
import com.sena.backend.entity.Role;
import com.sena.backend.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Endpoints for managing user roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get all roles", description = "Retrieves a list of all available roles in the system. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of roles successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid")
    })
    @GetMapping
    public ResponseEntity<List<Role>> list() {
        return ResponseEntity.ok(roleService.listRoles());
    }

    @Operation(summary = "Create a new role", description = "Creates a new user role in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient role permissions")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Role> create(@Valid @RequestBody CreateRoleRequest req) {
        Role r = roleService.createRole(req.getName(), req.getDescription());
        return ResponseEntity.status(201).body(r);
    }
}