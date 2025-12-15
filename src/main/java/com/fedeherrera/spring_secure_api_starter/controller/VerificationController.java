package com.fedeherrera.spring_secure_api_starter.controller;

import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;
import com.fedeherrera.spring_secure_api_starter.service.UserService;
import com.fedeherrera.spring_secure_api_starter.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final UserService userService;

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String token) {
        User user = verificationService.validateToken(token)
                .orElseThrow(() -> new RegistrationException("Token inválido o expirado"));

        user.setEnabled(true);
        userService.save(user);

        verificationService.deleteToken(token);

        return ResponseEntity.ok("Cuenta verificada correctamente");
    }
}
