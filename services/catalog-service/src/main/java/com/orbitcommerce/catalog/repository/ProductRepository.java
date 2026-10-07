package com.orbitcommerce.catalog.repository;

import com.orbitcommerce.catalog.model.Product;
import com.orbitcommerce.catalog.response.ProductListItemResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    @Query(
            value = """
                    SELECT new com.orbitcommerce.catalog.response.ProductListItemResponse(
                        product.id,
                        product.sku,
                        product.name,
                        product.description,
                        product.brand,
                        category.slug,
                        MIN(variantPrice.priceCents),
                        :currency
                    )
                    FROM Product product
                    JOIN product.category category
                    JOIN product.variants variant
                    JOIN variant.variantPrice variantPrice
                    WHERE variantPrice.currency = :currency
                      AND variantPrice.effectiveFrom <= CURRENT_TIMESTAMP
                      AND (variantPrice.effectiveTo IS NULL
                           OR variantPrice.effectiveTo > CURRENT_TIMESTAMP)
                      AND (:categorySlug IS NULL OR category.slug = :categorySlug)
                      AND (:query IS NULL
                           OR LOWER(product.sku) LIKE :query
                           OR LOWER(product.name) LIKE :query
                           OR LOWER(product.description) LIKE :query
                           OR LOWER(product.brand) LIKE :query)
                    GROUP BY product.id, product.sku, product.name, product.description,
                             product.brand, category.slug
                    ORDER BY product.name ASC, product.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT product.id)
                    FROM Product product
                    JOIN product.category category
                    JOIN product.variants variant
                    JOIN variant.variantPrice variantPrice
                    WHERE variantPrice.currency = :currency
                      AND variantPrice.effectiveFrom <= CURRENT_TIMESTAMP
                      AND (variantPrice.effectiveTo IS NULL
                           OR variantPrice.effectiveTo > CURRENT_TIMESTAMP)
                      AND (:categorySlug IS NULL OR category.slug = :categorySlug)
                      AND (:query IS NULL
                           OR LOWER(product.sku) LIKE :query
                           OR LOWER(product.name) LIKE :query
                           OR LOWER(product.description) LIKE :query
                           OR LOWER(product.brand) LIKE :query)
                    """
    )
    Page<ProductListItemResponse> findCatalogPage(
            @Param("categorySlug") String categorySlug,
            @Param("query") String query,
            @Param("currency") String currency,
            Pageable pageable
    );

}
