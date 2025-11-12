package ru.ifmo.highload.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;

@FeignClient(name = "catalog-service", path = "/api/product")
public interface ProductServiceClient {

    @GetMapping("/{id}")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductByIdFallback")
    ProductResponse getProductById(@PathVariable Long id);

    @PutMapping("/{id}")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "updateProductFallback")
    ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request);

    default ProductResponse getProductByIdFallback(Long id, Exception e) {
        throw new RuntimeException("Product service unavailable: " + e.getMessage());
    }

    default ProductResponse updateProductFallback(Long id, ProductUpdateRequest request, Exception e) {
        throw new RuntimeException("Product service unavailable: " + e.getMessage());
    }
}

