package com.orbitcommerce.identity.dto;

import org.springframework.beans.factory.annotation.Value;

public record DataTokenDTO(
        String token_type,
        String access_token,
        long expires_in,
        String refresh_token
) {
}
