package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record PriceEffectiveToRequest(Instant effectiveTo, @NotBlank String currency) {
}
