package com.fedeherrera.spring_secure_api_starter.service;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

   @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verificación de cuenta";
        String link = "https://tuapp.com/verify?token=" + token;
        String text = "Bienvenido! Haz click en el link para verificar tu cuenta: " + link;

        sendEmail(to, subject, text);
    }


   @Override
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Restablecer contraseña";
        String link = "https://tuapp.com/reset-password?token=" + token;
        String text = "Has solicitado restablecer tu contraseña. Haz click en el link: " + link
                    + "\nEl enlace expirará en 1 hora.";

        sendEmail(to, subject, text);
    }
}
