package com.fedeherrera.spring_secure_api_starter.repository;

import com.fedeherrera.spring_secure_api_starter.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByUserId(Long userId);
    void deleteByToken(String token);
}
