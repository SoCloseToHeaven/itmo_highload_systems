package ru.ifmo.highload.price.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.ifmo.highload.price.dto.external.product.ProductResponse;

@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/product/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}

