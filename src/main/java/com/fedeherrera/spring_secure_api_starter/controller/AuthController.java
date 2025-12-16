package com.fedeherrera.spring_secure_api_starter.controller;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fedeherrera.spring_secure_api_starter.dto.AdminCreateUserRequest;
import com.fedeherrera.spring_secure_api_starter.dto.DTOResetPassword;
import com.fedeherrera.spring_secure_api_starter.dto.EmailReset;
import com.fedeherrera.spring_secure_api_starter.dto.LoginRequest;
import com.fedeherrera.spring_secure_api_starter.dto.PublicRegisterRequest;
import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;
import com.fedeherrera.spring_secure_api_starter.service.AuthService;
import com.fedeherrera.spring_secure_api_starter.service.UserService;
import com.fedeherrera.spring_secure_api_starter.service.VerificationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "APIs para autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;
    private final UserService userService;

    @Operation(summary = "Registro público de usuario", description = "Permite a un nuevo usuario registrarse en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", 
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El correo electrónico ya está en uso")
    })
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody PublicRegisterRequest request) {
        authService.registerPublic(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully. Please verify your email.");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Crear usuario (Admin)", description = "Permite a un administrador crear un nuevo usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: se requiere rol ADMIN")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        authService.registerInternal(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully. Please verify your email.");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Solicitar restablecimiento de contraseña", 
              description = "Envía un correo con un enlace para restablecer la contraseña")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Si el correo existe, se enviarán instrucciones",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Correo electrónico inválido")
    })
    @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestReset(@RequestBody @Valid EmailReset emailReset) {
        User user = userService.findByEmail(emailReset.getEmail())
                .orElseThrow(() -> new RegistrationException("Email no encontrado"));
        verificationService.createPasswordResetToken(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Si tu email está registrado, recibirás un link para restablecer tu contraseña");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Restablecer contraseña", 
              description = "Restablece la contraseña usando el token de verificación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Token inválido o expirado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resetPassword(@RequestBody @Valid DTOResetPassword resetPassword) {
        userService.resetPassword(resetPassword.getToken(), resetPassword.getNewPassword());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña actualizada correctamente");
        return ResponseEntity.ok(response);
    }
}