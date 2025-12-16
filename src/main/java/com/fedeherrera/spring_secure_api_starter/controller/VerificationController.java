package com.fedeherrera.spring_secure_api_starter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;
import com.fedeherrera.spring_secure_api_starter.service.UserService;
import com.fedeherrera.spring_secure_api_starter.service.VerificationService;

@Tag(
    name = "Autenticacion", 
    description = "Endpoints para autenticación y verificación de usuarios"
)
@RestController 
@RequestMapping("/auth")    
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final UserService userService;

    @Operation(
        summary = "Verificar cuenta de usuario",
        description = "Verifica la cuenta de un usuario utilizando un token de verificación enviado por correo electrónico",
        parameters = {
            @Parameter(
                name = "token",
                description = "Token de verificación enviado al correo del usuario",
                required = true,
                example = "abc123xyz"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Cuenta verificada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = Map.class, 
                    example = "{\"mensaje\": \"Cuenta verificada correctamente\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Token inválido o expirado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content
        )
    })
    @GetMapping("/verify")
    public ResponseEntity<?> verificarCuenta(@RequestParam String token) {
        User user = verificationService.validateToken(token)
                .orElseThrow(() -> new RegistrationException("Token inválido o expirado"));

        user.setEnabled(true);
        userService.save(user);

        verificationService.deleteToken(token);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Cuenta verificada correctamente");

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}