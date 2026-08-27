package com.sena.backend.service.impl;

import com.sena.backend.domain.AuthResponse;
import com.sena.backend.domain.LoginRequest;
import com.sena.backend.entity.User;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.security.JwtUtil;
import com.sena.backend.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final Argon2PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, Argon2PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        // Retrieve user or throw exception with Spanish message
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
                
        // Verify password or throw exception with Spanish message
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        
        // Check active status
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("La cuenta de usuario está inactiva: " + user.getUsername());
        } else {
            String role = user.getRole().getName();
            
            // Generate token and handle potential generation errors
            try {
                String token = jwtUtil.generateToken(user.getUsername(), role);
                return new AuthResponse(token);
            } catch (Exception e) {
                throw new RuntimeException("Error al generar el token de acceso", e);
            }
        }
    }
}