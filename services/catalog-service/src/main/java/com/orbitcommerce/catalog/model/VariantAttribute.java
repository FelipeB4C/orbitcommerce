package com.orbitcommerce.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "variant_attributes",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_variant_attributes_variant_name",
                    columnNames = {"product_variant_id", "attribute_name"}
            )
        })
public class VariantAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "attribute_name", nullable = false)
    @Size(min = 1, max = 60)
    @NotBlank
    private String attributeName;

    @Column(name = "attribute_value", nullable = false)
    @Size(min = 1, max = 60)
    @NotBlank
    private String attributeValue;

    public VariantAttribute(String attributeName, String attributeValue) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }
}
