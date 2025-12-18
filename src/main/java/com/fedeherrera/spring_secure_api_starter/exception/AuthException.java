package com.fedeherrera.spring_secure_api_starter.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final HttpStatus status;

    public AuthException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED; // Por defecto
    }

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}