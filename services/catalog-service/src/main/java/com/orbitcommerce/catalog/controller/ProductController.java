package com.orbitcommerce.catalog.controller;

import com.orbitcommerce.catalog.request.CreateProductRequest;
import com.orbitcommerce.catalog.request.PriceEffectiveToRequest;
import com.orbitcommerce.catalog.request.PriceVariantUpdateRequest;
import com.orbitcommerce.catalog.response.ProductDetailResponse;
import com.orbitcommerce.catalog.response.ProductListItemResponse;
import com.orbitcommerce.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductListItemResponse>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "CAD") String currency
    ) {
        return ResponseEntity.ok(
                productService.listProducts(page, size, categorySlug, query, currency)
        );
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponse> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest,
                                           UriComponentsBuilder uriBuilder) {
        ProductDetailResponse product = productService.saveProduct(createProductRequest);
        URI uri =  uriBuilder.path("/product/{id}").buildAndExpand(product.id()).toUri();
        return ResponseEntity.created(uri).body(product);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductDetailResponse> findProductBySku(@PathVariable String sku) {
        productService.findProductBySku(sku);
        return ResponseEntity.ok().body(productService.findProductBySku(sku));
    }

    @PatchMapping("/{sku}/variants/{variantId}/prices")
    public ResponseEntity<Void> updatePriceHistory(@RequestBody @Valid
                                                   PriceVariantUpdateRequest request,
                                                   @PathVariable String sku,
                                                   @PathVariable String variantId) {
        productService.updatePriceVariant(request, sku, variantId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sku}/variants/{variantId}/effectiveto")
    public ResponseEntity<Void> updatePriceHistory(@RequestBody @Valid
                                                       PriceEffectiveToRequest request,
                                                   @PathVariable String sku,
                                                   @PathVariable String variantId) {
        productService.updatePriceEffectiveTo(request, sku, variantId);
        return ResponseEntity.noContent().build();
    }


}
