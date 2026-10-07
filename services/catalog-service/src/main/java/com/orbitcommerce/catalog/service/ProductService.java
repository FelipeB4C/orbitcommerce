package com.orbitcommerce.catalog.service;

import com.orbitcommerce.catalog.exception.BusinessException;
import com.orbitcommerce.catalog.exception.SkuAlreadyExistException;
import com.orbitcommerce.catalog.mapper.ProductMapper;
import com.orbitcommerce.catalog.model.Category;
import com.orbitcommerce.catalog.model.VariantPrice;
import com.orbitcommerce.catalog.model.Product;
import com.orbitcommerce.catalog.model.ProductVariant;
import com.orbitcommerce.catalog.repository.CategoryRepository;
import com.orbitcommerce.catalog.repository.ProductRepository;
import com.orbitcommerce.catalog.request.CreateProductRequest;
import com.orbitcommerce.catalog.request.PriceEffectiveToRequest;
import com.orbitcommerce.catalog.request.PriceVariantUpdateRequest;
import com.orbitcommerce.catalog.response.ProductDetailResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDetailResponse saveProduct(CreateProductRequest createProductRequest) {
        Category category = categoryRepository.findById(UUID.fromString(createProductRequest.categoryId()))
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if(productRepository.findBySku(createProductRequest.sku()).isPresent()) {
            throw new SkuAlreadyExistException("Product with sku " + createProductRequest.sku() + " already exists");
        }

        Product productEntity = productMapper.toProductEntity(createProductRequest);
        productEntity.setCategory(category);

        Product productSaved = productRepository.save(productEntity);

        return productMapper.toProductDetailResponse(productSaved);
    }


    public ProductDetailResponse findProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException("Product not found"));
        return productMapper.toProductDetailResponse(product);
    }


    @Transactional
    public void updatePriceVariant(PriceVariantUpdateRequest request, String sku, String variantId) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException("Product not found"));

        UUID id = UUID.fromString(variantId);
        ProductVariant variant = product.getVariants().stream()
                .filter(productVariant -> productVariant.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));


        Optional<VariantPrice> activePrice = variant.getVariantPrice().stream()
                .filter(history -> history.getEffectiveTo() == null
                        && history.getCurrency().equals(request.currency()))
                .findFirst();


        // Verify if the value prince in the request is the same of the registry
        if (activePrice.isPresent()
                && activePrice.get().getPriceCents().equals(request.priceCents())) {
            return;
        }

        if (activePrice.isPresent()) {
            activePrice.get().updatePriceHistoryEffectiveTo(Instant.now());
            productRepository.flush();
        }

        variant.addPrice(request.currency(), request.priceCents());
    }

    @Transactional
    public void updatePriceEffectiveTo(PriceEffectiveToRequest request, String sku, String variantId) {

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException("Product not found"));

        UUID id = UUID.fromString(variantId);
        ProductVariant variant = product.getVariants().stream()
                .filter(productVariant -> productVariant.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));


        VariantPrice activePrice = variant.getVariantPrice().stream()
                .filter(history -> history.getEffectiveTo() == null
                        && history.getCurrency().equals(request.currency()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Active price not found"));;

        Instant effectiveTo = request.effectiveTo() != null
                ? request.effectiveTo()
                : Instant.now();

        if (!effectiveTo.isAfter(activePrice.getEffectiveFrom())) {
            throw new BusinessException("effectiveTo must be after effectiveFrom");
        }

        activePrice.updatePriceHistoryEffectiveTo(effectiveTo);
    }

}
