package com.orbitcommerce.identity.dto;

import com.orbitcommerce.identity.model.Role;

import java.util.Set;
import java.util.UUID;

public record UserDetailDTO(
        UUID id,
        String fullName,
        String email,
        Set<Role> roles,
        String status,
        String createdAt
) {
}
