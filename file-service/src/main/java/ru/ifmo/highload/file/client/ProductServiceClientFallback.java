package ru.ifmo.highload.file.client;

import org.springframework.stereotype.Component;
import ru.ifmo.highload.file.dto.external.ProductResponse;

@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductResponse getProductById(Long id) {
        return null;
    }
}
