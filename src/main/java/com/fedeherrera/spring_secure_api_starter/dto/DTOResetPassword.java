package com.fedeherrera.spring_secure_api_starter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DTOResetPassword {
    
    
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
