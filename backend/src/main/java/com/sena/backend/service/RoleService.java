package com.sena.backend.service;

import com.sena.backend.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role createRole(String name, String description);
    List<Role> listRoles();
}
