package com.sena.backend.service.impl;

import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.doctor.DoctorResponse;
import com.sena.backend.domain.doctor.UpdateDoctorRequest;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.User;
import com.sena.backend.entity.Role;

import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.service.UserService;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.specification.DoctorSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        User user = internalCreateUser(req.getUsername(), req.getPassword(), "ROLE_DOCTOR");

        Doctor doctor = Doctor.builder()
                .documentNumber(req.getDocumentNumber())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .specialty(req.getSpecialty())
                .user(user)
                .build();

        doctorRepository.save(doctor);
    }

    private User internalCreateUser(String username, String password, String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("Username already exists: " + username);
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

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado con ID: " + id));
        return mapToDoctorResponse(doctor);
    }

    @Override
    @Transactional
    public void updateDoctor(Long id, UpdateDoctorRequest req) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado con ID: " + id));

        doctor.setFullName(req.getFullName());
        doctor.setEmail(req.getEmail());
        doctor.setPhone(req.getPhone());
        doctor.setSpecialty(req.getSpecialty());
        // If status changes to inactive, also disable user account to block login
        doctor.getUser().setActive(req.getIsActive());

        doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void toggleDoctorStatus(Long id, boolean status) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado con ID: " + id));

        doctor.getUser().setActive(status);
        doctorRepository.save(doctor);
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .documentNumber(doctor.getDocumentNumber())
                .fullName(doctor.getFullName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .specialty(doctor.getSpecialty())
                .username(doctor.getUser().getUsername())
                .isActive(doctor.getUser().getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(int page, int size, String sortBy, String fullName, String specialty, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        // Build dynamic query based on received parameters
        Specification<Doctor> spec = DoctorSpecification.withDynamicFilters(fullName, specialty, isActive);

        return doctorRepository.findAll(spec, pageable)
                .map(this::mapToDoctorResponse);
    }

}