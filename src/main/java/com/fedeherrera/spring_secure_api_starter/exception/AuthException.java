package com.fedeherrera.spring_secure_api_starter.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
