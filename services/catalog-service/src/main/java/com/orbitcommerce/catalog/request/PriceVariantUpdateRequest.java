package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PriceVariantUpdateRequest(@NotNull Long priceCents, @NotBlank String currency) {
}
