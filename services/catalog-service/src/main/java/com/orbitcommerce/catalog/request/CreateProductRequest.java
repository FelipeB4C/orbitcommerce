package com.orbitcommerce.catalog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.HashSet;

public record CreateProductRequest(
        @NotBlank
        String sku,
        @NotBlank
        String name,
        String description,
        @NotBlank
        String categoryId,
        String brand,
        @NotEmpty
        HashSet<ProductVariantRequest> variants
) {
}
