package com.orbitcommerce.identity.dto;

import java.util.UUID;

public record UserRegisterResponseDTO(
        UUID id,
        String fullName,
        String email,
        String status,
        String createdAt
) {
}
