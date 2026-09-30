package com.orbitcommerce.identity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDTO(@NotBlank @Valid String newPassword, @NotBlank @Valid String oldPassword) {
}
