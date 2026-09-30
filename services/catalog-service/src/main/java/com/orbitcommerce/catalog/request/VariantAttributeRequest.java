package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;

public record VariantAttributeRequest(@NotBlank String name, @NotBlank String value) {
}
