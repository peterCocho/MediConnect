package com.sena.backend.service.impl;

import com.sena.backend.domain.AuthResponse;
import com.sena.backend.domain.LoginRequest;
import com.sena.backend.entity.Role;
import com.sena.backend.entity.User;
import com.sena.backend.repository.UserRepository;
import com.sena.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl#login.
 *
 * Covers HU-01 (Autenticación de Usuarios): credenciales inválidas (usuario
 * inexistente o password incorrecto), cuenta inactiva, y generación exitosa
 * del token JWT.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - login")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Argon2PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .id(1)
                .name("ROLE_RECEPTION")
                .description("Recepcionista")
                .build();

        activeUser = User.builder()
                .id(10L)
                .username("recepcion1")
                .password("hashed-password")
                .role(role)
                .isActive(true)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("recepcion1");
        loginRequest.setPassword("plain-password");
    }

    @Test
    @DisplayName("Con credenciales válidas retorna un AuthResponse con token JWT")
    void login_withValidCredentials_returnsToken() throws Exception {
        when(userRepository.findByUsername("recepcion1")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken("recepcion1", "ROLE_RECEPTION")).thenReturn("fake.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        verify(jwtUtil).generateToken("recepcion1", "ROLE_RECEPTION");
    }

    @Test
    @DisplayName("Con usuario inexistente lanza BadCredentialsException")
    void login_withUnknownUsername_throwsBadCredentials() {
        when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());
        loginRequest.setUsername("fantasma");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Con password incorrecto lanza BadCredentialsException")
    void login_withWrongPassword_throwsBadCredentials() {
        when(userRepository.findByUsername("recepcion1")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Con usuario inactivo lanza UsernameNotFoundException")
    void login_withInactiveUser_throwsUsernameNotFound() {
        activeUser.setActive(false);
        when(userRepository.findByUsername("recepcion1")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("recepcion1");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Si JwtUtil falla al generar el token, se envuelve en RuntimeException")
    void login_whenTokenGenerationFails_throwsRuntimeException() throws Exception {
        when(userRepository.findByUsername("recepcion1")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al generar el token de acceso");
    }
}
