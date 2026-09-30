package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;

public record ProductVariantRequest(
        @NotEmpty
        HashSet<VariantAttributeRequest> attributes,
        @NotNull
        Long priceCents,
        @NotBlank
        String currency,
        @NotBlank
        String stockKeepingUnit
) {
}
