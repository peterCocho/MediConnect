package com.sena.backend.service.impl;

import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.User;
import com.sena.backend.entity.Role;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.service.UserService;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           DoctorRepository doctorRepository,
                           Argon2PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void assignRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado en el sistema: " + roleName));

        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void createDoctor(CreateDoctorRequest req) {
        // 1. Crear el usuario con ROLE_DOCTOR
        User user = internalCreateUser(req.getUsername(), req.getPassword(), "ROLE_DOCTOR");

        // 2. Crear el perfil del doctor vinculado al usuario
        Doctor doctor = Doctor.builder()
                .documentNumber(req.getDocumentNumber())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .specialty(req.getSpecialty())
                .isActive(true)
                .user(user)
                .build();

        doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void createReceptionist(CreateReceptionistRequest req) {
        // Para recepcionistas, solo creamos el usuario con ROLE_RECEPTION
        internalCreateUser(req.getUsername(), req.getPassword(), "ROLE_RECEPTION");
    }
    

    /**
     * Helper para la creación base de un usuario persistido.
     */
    private User internalCreateUser(String username, String password, String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("El nombre de usuario ya existe: " + username);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }
}