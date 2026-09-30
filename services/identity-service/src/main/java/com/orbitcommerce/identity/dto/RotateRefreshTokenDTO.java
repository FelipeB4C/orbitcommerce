package com.orbitcommerce.identity.dto;

import java.util.UUID;

public record RotateRefreshTokenDTO(String newRefreshToken, UUID userId) {
}
