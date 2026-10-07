package com.orbitcommerce.catalog.response;

import java.util.UUID;

public record ProductListItemResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String brand,
        String categorySlug,
        Long priceCents,
        String currency
) {
}
