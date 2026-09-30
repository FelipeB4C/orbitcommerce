package com.orbitcommerce.catalog.response;

public record ProductImageResponse(
        String url,
        String altText,
        Integer position
) {
}
