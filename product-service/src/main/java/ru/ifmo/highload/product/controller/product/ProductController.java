package ru.ifmo.highload.product.controller.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.product.api.ProductService;
import ru.ifmo.highload.product.dto.product.ProductResponse;
import ru.ifmo.highload.product.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.product.dto.stats.ProductPurchaseStatsResponse;
import ru.ifmo.highload.product.impl.stats.ProductPurchaseStatsService;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;
    private final ProductPurchaseStatsService productPurchaseStatsService;

    @Override
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public ResponseEntity<ProductResponse> updateProduct(Long id, ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Override
    public ResponseEntity<Page<ProductResponse>> searchProducts(String name, Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(name, pageable));
    }

    @Override
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllCategories(pageable));
    }

    @Override
    public ResponseEntity<List<ProductPurchaseStatsResponse>> getProductPurchaseStats() {
        return ResponseEntity.ok(productPurchaseStatsService.getAllStats());
    }
}

