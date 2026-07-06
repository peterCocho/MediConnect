package com.sena.backend.controller;

import com.sena.backend.domain.role.CreateRoleRequest;
import com.sena.backend.entity.Role;
import com.sena.backend.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> list() {
        return ResponseEntity.ok(roleService.listRoles());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Role> create(@Valid @RequestBody CreateRoleRequest req) {
        Role r = roleService.createRole(req.getName(), req.getDescription());
        return ResponseEntity.status(201).body(r);
    }
}
