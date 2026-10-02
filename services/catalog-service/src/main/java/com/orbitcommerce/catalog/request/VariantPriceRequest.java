package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VariantPriceRequest(@NotBlank String currency, @NotNull Long priceCents) {
}
