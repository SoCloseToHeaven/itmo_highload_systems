package ru.ifmo.highload.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.ifmo.highload.dto.product.ProductResponse;

@FeignClient(name = "catalog-service", path = "/api/product")
public interface ProductServiceClient {

    @GetMapping("/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    ProductResponse getProductById(@PathVariable Long id);

    default ProductResponse getProductByIdFallback(Long id, Exception e) {
        throw new RuntimeException("Product service unavailable: " + e.getMessage());
    }
}

