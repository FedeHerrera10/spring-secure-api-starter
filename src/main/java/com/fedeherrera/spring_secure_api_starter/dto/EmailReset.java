
package com.fedeherrera.spring_secure_api_starter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailReset  {

    @Email
    @NotBlank
    private String email;

}   

