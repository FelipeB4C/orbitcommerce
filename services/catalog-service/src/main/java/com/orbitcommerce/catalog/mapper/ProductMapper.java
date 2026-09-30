package com.orbitcommerce.catalog.mapper;

import com.orbitcommerce.catalog.model.*;
import com.orbitcommerce.catalog.request.CreateProductRequest;
import com.orbitcommerce.catalog.request.ProductVariantRequest;
import com.orbitcommerce.catalog.request.VariantAttributeRequest;
import com.orbitcommerce.catalog.response.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", source = "request.variants")
    Product toProductEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "priceHistory", ignore = true)
    @Mapping(target = "variantAttributes", source = "attributes")
    ProductVariant toVariantEntity(ProductVariantRequest variantRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "attributeName", source = "name")
    @Mapping(target = "attributeValue", source = "value")
    VariantAttribute toAttributeEntity(VariantAttributeRequest attributeRequest);

    @AfterMapping
    default void linkBidirectionalRelationships(@MappingTarget Product product) {
        if (product.getVariants() == null) return;

        product.getVariants().forEach(variant -> {
            variant.setProduct(product);

            // Garante a bidirecionalidade dos atributos existentes usando o método auxiliar
            if (variant.getVariantAttributes() != null) {
                variant.getVariantAttributes().forEach(attr -> attr.setProductVariant(variant));
            }

            // Delega a criação da regra de preço inicial para a entidade
            variant.addInitialPrice();
        });
    }

    ProductDetailResponse toProductDetailResponse(Product product);

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "attributes", source = "variantAttributes")
    @Mapping(target = "sku", source = "stockKeepingUnit")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    @Mapping(target = "name", source = "attributeName")
    @Mapping(target = "value", source = "attributeValue")
    VariantAttributeResponse toVariantAttributeResponse(VariantAttribute variantAttribute);

    ProductImageResponse toProductImageResponse(ProductImage product);

}
