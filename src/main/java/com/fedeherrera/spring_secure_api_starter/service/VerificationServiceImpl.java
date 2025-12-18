package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.entity.VerificationToken;
import com.fedeherrera.spring_secure_api_starter.entity.VerificationToken.TokenType;
import com.fedeherrera.spring_secure_api_starter.repository.VerificationTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public VerificationToken createToken(User user) {
        String tokenStr = UUID.randomUUID().toString();

        VerificationToken token = VerificationToken.builder()
                .token(tokenStr)
                .user(user)
                .type(TokenType.VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(24)) // expira en 24h
                .build();

        return tokenRepository.save(token);
    }

    @Override
    public Optional<User> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(VerificationToken::getUser)
                .filter(u -> u != null); // ⚡ asegurarse de no devolver null
    }

    @Override
    @Transactional
    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }

    @Override
    @Transactional
public void createPasswordResetToken(User user) {
    String tokenStr = UUID.randomUUID().toString();

    VerificationToken token = VerificationToken.builder()
            .token(tokenStr)
            .user(user)
            .type(VerificationToken.TokenType.PASSWORD_RESET)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .createdAt(LocalDateTime.now())
            .build();

    tokenRepository.save(token);

    // Reutilizamos EmailService
    emailService.sendPasswordResetEmail(user.getEmail(), tokenStr);
}

public Optional<User> validatePasswordResetToken(String token) {
    return tokenRepository.findByToken(token)
            .filter(t -> t.getType() == VerificationToken.TokenType.PASSWORD_RESET)
            .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(VerificationToken::getUser);
}



 

}
