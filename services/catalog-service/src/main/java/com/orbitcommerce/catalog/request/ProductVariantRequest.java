package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.HashSet;

public record ProductVariantRequest(
        @NotEmpty
        HashSet<VariantAttributeRequest> attributes,
        @NotEmpty
        HashSet<VariantPriceRequest> prices,
        @NotBlank
        String stockKeepingUnit
) {
}
