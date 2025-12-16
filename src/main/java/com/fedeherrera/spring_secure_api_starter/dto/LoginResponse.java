package com.fedeherrera.spring_secure_api_starter.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String username;
    private String accessToken;
    private String refreshToken; // opcional, si usas JWT con refresh
    private String role;
}
