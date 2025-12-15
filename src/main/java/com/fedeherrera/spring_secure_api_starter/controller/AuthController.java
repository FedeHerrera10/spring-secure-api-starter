package com.fedeherrera.spring_secure_api_starter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fedeherrera.spring_secure_api_starter.dto.AdminCreateUserRequest;
import com.fedeherrera.spring_secure_api_starter.dto.DTOResetPassword;
import com.fedeherrera.spring_secure_api_starter.dto.EmailReset;
import com.fedeherrera.spring_secure_api_starter.dto.PublicRegisterRequest;
import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;
import com.fedeherrera.spring_secure_api_starter.service.AuthService;
import com.fedeherrera.spring_secure_api_starter.service.UserService;
import com.fedeherrera.spring_secure_api_starter.service.VerificationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody PublicRegisterRequest request) {
        authService.registerPublic(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully. Please verify your email.");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        authService.registerInternal(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully. Please verify your email.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestReset(@RequestBody @Valid EmailReset emailReset) {
        User user = userService.findByEmail(emailReset.getEmail())
                .orElseThrow(() -> new RegistrationException("Email no encontrado"));
        verificationService.createPasswordResetToken(user);
        return ResponseEntity.ok("Si tu email está registrado, recibirás un link para restablecer tu contraseña");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid DTOResetPassword resetPassword) {
        userService.resetPassword(resetPassword.getToken() , resetPassword.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

   

}
