package com.fedeherrera.spring_secure_api_starter.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
}
