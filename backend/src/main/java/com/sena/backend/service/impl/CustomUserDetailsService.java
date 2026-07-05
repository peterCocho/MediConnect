package com.sena.backend.service.impl;

import com.sena.backend.entity.User;
import com.sena.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));


        String authority = (appUser.getRole() != null && appUser.getRole().getName() != null && appUser.getRole().getName().startsWith("ROLE_"))
                ? appUser.getRole().getName()
                : "ROLE_" + (appUser.getRole() != null ? appUser.getRole().getName() : "USER");

        return new org.springframework.security.core.userdetails.User(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.getIsActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}