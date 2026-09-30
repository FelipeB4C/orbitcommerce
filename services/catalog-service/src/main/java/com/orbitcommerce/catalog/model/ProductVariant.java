package com.orbitcommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "price_cents")
    @NotNull
    private Long priceCents;

    @Column(nullable = false, length = 3)
    @NotNull
    private String currency = "CAD";

    @Column(name = "stock_keeping_unit", unique = true, nullable = false)
    @NotBlank
    private String stockKeepingUnit;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantAttribute> variantAttributes = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceHistory> priceHistory = new ArrayList<>();


    public ProductVariant(Product product, Long priceCents, String currency, String stockKeepingUnit) {
        this.product = product;
        this.priceCents = priceCents;
        this.currency = currency;
        this.stockKeepingUnit = stockKeepingUnit;
    }

    public List<PriceHistory> getPriceHistory() {
        return priceHistory;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void addInitialPrice() {
        PriceHistory initialPrice = new PriceHistory(
                this,
                this.getPriceCents(),
                Instant.now(),
                null);
        this.priceHistory.add(initialPrice);
    }

}
