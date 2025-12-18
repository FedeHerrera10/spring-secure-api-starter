package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.entity.VerificationToken;

    import java.util.Optional;

public interface VerificationService {

    /**
     * Genera un token de verificación para un usuario.
     */
    VerificationToken createToken(User user);

    /**
     * Valida un token y devuelve el usuario si es válido y no expiró.
     */
    Optional<User> validateToken(String token);

    /**
     * Elimina un token de la base de datos.
     */
    void deleteToken(String token);

    Optional<User> validatePasswordResetToken(String token);
    
    void createPasswordResetToken(User user);

}
