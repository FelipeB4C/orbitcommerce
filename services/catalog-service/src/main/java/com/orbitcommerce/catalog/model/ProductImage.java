package com.orbitcommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Size(min = 1, max = 500)
    private String url;

    @Column(name = "alt_text")
    @Size(min = 1, max = 150)
    private String altText;

    @NotNull
    private Integer position = 0;


    public ProductImage(Product product, String url, String altText) {
        this.product = product;
        this.url = url;
        this.altText = altText;
    }
}
