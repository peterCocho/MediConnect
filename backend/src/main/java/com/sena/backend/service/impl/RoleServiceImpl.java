package com.sena.backend.service.impl;

import com.sena.backend.entity.Role;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(String name, String description) {
        Role r = new Role();
        r.setName(name);
        r.setDescription(description);
        return roleRepository.save(r);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }
}
