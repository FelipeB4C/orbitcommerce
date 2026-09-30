package com.orbitcommerce.catalog.service;

import com.orbitcommerce.catalog.exception.SkuAlreadyExistException;
import com.orbitcommerce.catalog.mapper.ProductMapper;
import com.orbitcommerce.catalog.model.Category;
import com.orbitcommerce.catalog.model.Product;
import com.orbitcommerce.catalog.repository.CategoryRepository;
import com.orbitcommerce.catalog.repository.ProductRepository;
import com.orbitcommerce.catalog.request.CreateProductRequest;
import com.orbitcommerce.catalog.response.ProductDetailResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

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


}
