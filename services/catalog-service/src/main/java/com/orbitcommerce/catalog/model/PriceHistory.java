package com.orbitcommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id",  nullable = false)
    private ProductVariant productVariant;

    @Positive
    @NotNull
    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    public PriceHistory(ProductVariant productVariant, Long priceCents, Instant effectiveFrom, Instant effectiveTo) {
        this.productVariant = productVariant;
        this.priceCents = priceCents;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

}
