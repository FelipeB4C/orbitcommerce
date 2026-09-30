package com.orbitcommerce.catalog.response;

import java.util.HashSet;

public record ProductVariantResponse(
        String id,
        HashSet<VariantAttributeResponse> attributes,
        Long priceCents,
        String currency,
        String sku
) {
}
