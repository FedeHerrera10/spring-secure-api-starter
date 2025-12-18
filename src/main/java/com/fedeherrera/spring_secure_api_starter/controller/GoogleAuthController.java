package com.fedeherrera.spring_secure_api_starter.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedeherrera.spring_secure_api_starter.dto.GoogleLoginRequest;
import com.fedeherrera.spring_secure_api_starter.dto.LoginResponse;
import com.fedeherrera.spring_secure_api_starter.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador que maneja la autenticación a través de Google.
 */
@Tag(name = "Autenticacion", description = "APIs para autenticación y registro de usuarios")
@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private final AuthService authService;

    public GoogleAuthController(
        AuthService authService
    ) {
        this.authService = authService;
    }

    @Operation(
        summary = "Iniciar sesión con Google",
        description = "Autentica a un usuario utilizando un token de identificación de Google"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inicio de sesión exitoso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de Google inválido o expirado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida",
            content = @Content
        )
    })
    @PostMapping("/login")
    public LoginResponse login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token de identificación de Google",
            required = true,
            content = @Content(schema = @Schema(implementation = GoogleLoginRequest.class))
        )
        @RequestBody GoogleLoginRequest request
    ) {
        return authService.loginWithGoogle(request.idToken());
    }
}