package com.orbitcommerce.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(@NotBlank String refreshToken) {
}
