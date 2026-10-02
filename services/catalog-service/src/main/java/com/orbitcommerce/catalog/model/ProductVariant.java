package com.orbitcommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "stock_keeping_unit", unique = true, nullable = false)
    @NotBlank
    private String stockKeepingUnit;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantAttribute> variantAttributes = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantPrice> variantPrice = new ArrayList<>();


    public ProductVariant(Product product, String stockKeepingUnit) {
        this.product = product;
        this.stockKeepingUnit = stockKeepingUnit;
    }

    public List<VariantPrice> getVariantPrice() {
        return variantPrice;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void addInitialPrice(String currency, Long priceCents) {
        VariantPrice initialPrice = new VariantPrice(this, currency, priceCents);
        this.variantPrice.add(initialPrice);
    }

    public void addPrice(String currency, Long priceCents) {
        VariantPrice price = new VariantPrice(this, currency, priceCents);
        this.variantPrice.add(price);
    }

}
