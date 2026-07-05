package com.sena.backend.service.impl;

import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.domain.receptionist.ReceptionistResponse;
import com.sena.backend.domain.receptionist.UpdateReceptionistRequest;
import com.sena.backend.entity.Receptionist;
import com.sena.backend.entity.Role;
import com.sena.backend.entity.User;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ReceptionistRepository;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.service.ReceptionistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceptionistServiceImpl implements ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ReceptionistServiceImpl(ReceptionistRepository receptionistRepository,
                                   UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.receptionistRepository = receptionistRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void createReceptionist(CreateReceptionistRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BusinessRuleException("El nombre de usuario ya está en uso.");
        }
        if (receptionistRepository.findByIdentityDocument(req.getIdentityDocument()).isPresent()) {
            throw new BusinessRuleException("Ya existe un recepcionista con este documento de identidad.");
        }

        Role receptionRole = roleRepository.findByName("ROLE_RECEPTION")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ROLE_RECEPTION no encontrado en la base de datos."));

        User newUser = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(receptionRole)
                .isActive(true)
                .build();

        Receptionist receptionist = Receptionist.builder()
                .user(newUser)
                .identityDocument(req.getIdentityDocument())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .build();

        receptionistRepository.save(receptionist);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionistResponse getReceptionistById(Long id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepcionista no encontrado con ID: " + id));
        return mapToResponse(receptionist);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceptionistResponse> getAllReceptionists(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return receptionistRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void updateReceptionist(Long id, UpdateReceptionistRequest req) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepcionista no encontrado con ID: " + id));

        receptionist.setFullName(req.getFullName());
        receptionist.setPhone(req.getPhone());
        receptionist.setIdentityDocument(req.getIdentityDocument());

        receptionistRepository.save(receptionist);
    }

    @Override
    @Transactional
    public void toggleReceptionistStatus(Long id, boolean status) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recepcionista no encontrado con ID: " + id));

        receptionist.getUser().setActive(status);
        receptionistRepository.save(receptionist);
    }

    private ReceptionistResponse mapToResponse(Receptionist receptionist) {
        return ReceptionistResponse.builder()
                .id(receptionist.getId())
                .username(receptionist.getUser().getUsername())
                .identityDocument(receptionist.getIdentityDocument())
                .fullName(receptionist.getFullName())
                .phone(receptionist.getPhone())
                .isActive(receptionist.getUser().getIsActive())
                .build();
    }
}