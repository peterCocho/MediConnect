package com.sena.backend.service.impl;

import com.sena.backend.domain.AuthResponse;
import com.sena.backend.domain.LoginRequest;
import com.sena.backend.entity.User;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.security.JwtUtil;
import com.sena.backend.service.AuthService;
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
    public AuthResponse login(LoginRequest req) throws Exception {
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> new Exception("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) throw new Exception("Invalid credentials");
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token);
    }
}
