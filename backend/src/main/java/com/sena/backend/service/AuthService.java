package com.sena.backend.service;

import com.sena.backend.domain.AuthResponse;
import com.sena.backend.domain.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest req) throws Exception;
}
