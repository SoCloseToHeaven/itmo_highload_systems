package ru.ifmo.highload.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.ifmo.highload.order.dto.external.product.ProductResponse;
import ru.ifmo.highload.order.dto.external.product.ProductUpdateRequest;

@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/product/{id}")
    ProductResponse getProductById(@PathVariable Long id);

    @PutMapping("/api/product/{id}")
    ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request);
}

