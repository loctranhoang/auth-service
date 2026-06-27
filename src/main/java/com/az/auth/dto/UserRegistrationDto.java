package com.az.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegistrationDto(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        String firstName,
        String lastName
) {
}
