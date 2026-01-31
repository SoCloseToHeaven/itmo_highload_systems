package ru.ifmo.highload.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.order.dto.external.product.ProductResponse;
import ru.ifmo.highload.order.dto.external.product.ProductUpdateRequest;

@Slf4j
@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductResponse getProductById(Long id) {
        log.error("ProductServiceClient fallback called for productId: {}", id);
        throw new RuntimeException("Product service unavailable");
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.error("ProductServiceClient fallback called for productId: {}", id);
        throw new RuntimeException("Product service unavailable");
    }
}

