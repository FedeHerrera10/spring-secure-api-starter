package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.entity.User;
import com.fedeherrera.spring_secure_api_starter.exception.RegistrationException;
import com.fedeherrera.spring_secure_api_starter.repository.UserRepository;
import com.fedeherrera.spring_secure_api_starter.repository.VerificationTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
public void resetPassword(String token, String newPassword) {
    User user = verificationService.validatePasswordResetToken(token)
            .orElseThrow(() -> new RegistrationException("Token inválido o expirado"));

    user.setPassword(passwordEncoder.encode(newPassword));

    user.setPasswordChangedAt(LocalDateTime.now()); // 👈 Aquí invalidamos los tokens anteriores
    
    userRepository.save(user);

    verificationTokenRepository.deleteByToken(token);
    log.info("Contraseña actualizada para el usuario {}. Tokens previos invalidados.", user.getEmail());
    
}

}
