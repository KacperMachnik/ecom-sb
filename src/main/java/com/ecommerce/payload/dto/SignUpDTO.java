package com.ecommerce.payload.dto;

import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignUpDTO {
    @NotBlank
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    private String username;

    @NotBlank
    @Size(max = 50, message = "Email must be at most 50 characters long")
    @Email
    private String email;

    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 120, message = "Password must be at least 6 characters long")
    private String password;
}