package com.orbitcommerce.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
        @Size(min = 1, max = 150)
        @NotBlank String fullName,
        @Email
        @Size(min = 1, max = 180)
        @NotBlank String email,
        @Size(min = 1, max = 255)
        @NotBlank String password) {
}
