package com.orbitcommerce.identity.dto;

import java.util.UUID;

public record UserRegisteredPayload(
        UUID userId,
        String fullName,
        String email
) {
}
