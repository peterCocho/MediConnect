package com.sena.backend.service.impl;

import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.User;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository; // Required to fetch clinical IDs

    public CustomUserDetailsService(UserRepository userRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String authorityName = (appUser.getRole() != null && appUser.getRole().getName() != null && appUser.getRole().getName().startsWith("ROLE_"))
                ? appUser.getRole().getName()
                : "ROLE_" + (appUser.getRole() != null ? appUser.getRole().getName() : "USER");

        Long doctorId = null;
        Long patientId = null;

        // Map the internal entity ID to the security context based on the specific role
        if ("ROLE_DOCTOR".equals(authorityName)) {
            // Assumes DoctorRepository has a method to find the doctor by the associated User ID.
            Doctor doctor = doctorRepository.findByUserId(appUser.getId())
                    .orElseThrow(() -> new IllegalStateException("Data integrity error: User has ROLE_DOCTOR but no matching Doctor record exists in the database."));
            doctorId = doctor.getId();
        }

        // Return the extended CustomUserDetails instead of the generic Spring User
        return new CustomUserDetails(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.getIsActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(authorityName)),
                doctorId,
                patientId
        );
    }
}