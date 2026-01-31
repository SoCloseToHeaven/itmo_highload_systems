package ru.ifmo.highload.discount.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.discount.dto.external.product.ProductResponse;

@Slf4j
@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductResponse getProductById(Long id) {
        log.error("ProductServiceClient fallback called for productId: {}", id);
        throw new RuntimeException("Product service unavailable");
    }
}

