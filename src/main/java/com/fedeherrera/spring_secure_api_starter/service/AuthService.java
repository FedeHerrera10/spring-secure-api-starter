package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.dto.AdminCreateUserRequest;
import com.fedeherrera.spring_secure_api_starter.dto.PublicRegisterRequest;
import com.fedeherrera.spring_secure_api_starter.entity.Role;
import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.entity.VerificationToken;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailService emailService;
    /**
     * Registro de usuario base
     * - Password encriptado
     * - ROLE_USER asignado
     * - enabled = false (requiere verificación)
     */
    public void registerPublic(PublicRegisterRequest request) {

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

        // 1️⃣ Generar token de verificación
    VerificationToken token = verificationService.createToken(user);

    // 2️⃣ Construir magic link
    String verificationLink = "http://localhost:3000/auth/verify?token=" + token.getToken();

    // 3️⃣ Enviar email
    emailService.sendEmail(
            user.getEmail(),
            "Verify your account",
            "Click the link to verify your account: " + verificationLink
    );

    System.out.println("Verification email sent to: " + user.getEmail());
    }

    public void registerInternal(AdminCreateUserRequest request) {

        if (userService.existsByUsername(request.getUsername())) {
            throw new RegistrationException("Username ya registrado.");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new RegistrationException("Email ya registrado.");
        }

        Set<Role> roles = request.getRoles().stream()
                .map(roleService::findByName)
                .map(opt -> opt.orElseThrow(() -> new RegistrationException("Invalid user data")))
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true) // admin puede habilitar
                .roles(roles)
                .build();

        userService.save(user);

        
    }

}
