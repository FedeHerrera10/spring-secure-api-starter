package com.fedeherrera.spring_secure_api_starter.service;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.fedeherrera.spring_secure_api_starter.config.GoogleAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

@Service
public class GoogleTokenVerifierService {

    private final GoogleAuthProperties properties;

    public GoogleTokenVerifierService(GoogleAuthProperties properties) {
        this.properties = properties;
    }

  public GoogleIdToken.Payload verify(String idTokenString) {
    try {
        // Verifica que el token no sea nulo o vacío
        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            throw new BadCredentialsException("El token no puede estar vacío");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
        .setAudience(List.of(properties.getId()))
        .build();

        // Verificación del token
        GoogleIdToken idToken = verifier.verify(idTokenString);
        
        if (idToken == null) {
            throw new BadCredentialsException("No se pudo verificar el token de Google");
        }

        // Verificación adicional de la audiencia
        GoogleIdToken.Payload payload = idToken.getPayload();
        if (!payload.getAudienceAsList().contains(properties.getId())) {
            throw new BadCredentialsException("El token no es para esta aplicación");
        }

        return payload;

    } catch (Exception e) {
        // Agrega más detalles del error
        String errorMsg = String.format(
            "Error al verificar el token: %s. Cliente ID configurado: %s",
            e.getMessage(),
            properties.getId()
        );
        throw new BadCredentialsException(errorMsg, e);
    }
}
}
