package com.orbitcommerce.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record DataLoginDTO(
        @NotBlank String email,
        @NotBlank String password
) {
}
