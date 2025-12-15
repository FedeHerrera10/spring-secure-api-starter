package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.dto.RegisterRequest;
import com.fedeherrera.spring_secure_api_starter.entity.Role;
import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registro de usuario base
     * - Password encriptado
     * - ROLE_USER asignado
     * - enabled = false (requiere verificación)
     */
    public void register(RegisterRequest request) {

        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        if (userService.existsByUsername(username)) {
            throw new RegistrationException("Username ya registrado.");
        }
        if (userService.existsByEmail(email)) {
            throw new RegistrationException("Email ya registrado.");
        }

        Role roleUser = roleService.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .roles(Set.of(roleUser))
                .build();

        userService.save(user);
    }

}
