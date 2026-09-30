package com.orbitcommerce.catalog.controller;

import com.orbitcommerce.catalog.request.CreateProductRequest;
import com.orbitcommerce.catalog.response.ProductDetailResponse;
import com.orbitcommerce.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponse> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest,
                                           UriComponentsBuilder uriBuilder) {
        ProductDetailResponse product = productService.saveProduct(createProductRequest);
        URI uri =  uriBuilder.path("/product/{id}").buildAndExpand(product.id()).toUri();
        return ResponseEntity.created(uri).body(product);
    }

}
