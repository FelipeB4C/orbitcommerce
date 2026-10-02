package com.orbitcommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "variant_prices")
public class VariantPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id",  nullable = false)
    private ProductVariant productVariant;

    @NotBlank
    @Column(nullable = false)
    private String currency;

    @Positive
    @NotNull
    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @PrePersist
    protected void onCreate() {
        effectiveFrom = Instant.now();
    }

    public VariantPrice(ProductVariant productVariant, String currency, Long priceCents) {
        this.productVariant = productVariant;
        this.currency = currency;
        this.priceCents = priceCents;
    }

    public void updatePriceHistoryEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
    }


}
