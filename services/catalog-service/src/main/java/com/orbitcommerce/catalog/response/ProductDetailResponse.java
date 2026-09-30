package com.orbitcommerce.catalog.response;

import java.util.HashSet;

public record ProductDetailResponse(
    String id,
    String sku,
    String name,
    String description,
    String brand,
    String status,
    CategoryResponse category,
    HashSet<ProductVariantResponse> variants,
    HashSet<ProductImageResponse> images
) {
}
