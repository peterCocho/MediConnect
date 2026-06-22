package com.sena.backend.service.impl;

import com.sena.backend.entity.User; // Asegúrate de importar la clase User
import com.sena.backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List; // Importar List si es necesario

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. Buscar el usuario
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Construir las autoridades usando el nombre almacenado del rol
        List<GrantedAuthority> authorities;
        if (appUser.getRole() != null) {
            String authority = appUser.getRole().getName() != null && appUser.getRole().getName().startsWith("ROLE_")
                    ? appUser.getRole().getName()
                    : "ROLE_" + appUser.getRole().getName();
            authorities = List.of(new SimpleGrantedAuthority(authority));
        } else {
            authorities = List.of(); // Lista vacía si no hay rol
        }


        // 3. Construir el objeto UserDetails directamente (¡La forma correcta!)
        return new UserDetails() {
            @Override
            public String getUsername() {
                return appUser.getUsername();
            }

            @Override
            public String getPassword() {
                return appUser.getPassword();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                // Usamos las autoridades que calculamos arriba
                return authorities;
            }

            @Override
            public boolean isAccountNonExpired() { return true; }
            @Override
            public boolean isAccountNonLocked() { return true; }
            @Override
            public boolean isCredentialsNonExpired() { return true; }
            @Override
            public boolean isEnabled() { return appUser.getIsActive(); } // Usamos el campo isActive

        }; // El .build() es opcional aquí, pero se mantiene por consistencia si lo usas.
    }

    // El método z() puede permanecer como está o eliminarse si no es necesario
    public void z() {
        System.out.println("Este es el método z()");
    }
}
