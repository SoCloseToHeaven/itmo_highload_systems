package ru.ifmo.highload.order.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.order.api.ProductDataService;
import ru.ifmo.highload.order.dto.external.product.ProductResponse;
import ru.ifmo.highload.order.dto.external.product.ProductUpdateRequest;

@Component
@RequiredArgsConstructor
public class ProductServiceAdapter implements ProductDataService {

    private final ProductServiceClient productServiceClient;

    @Override
    public ProductResponse getProductById(Long id) {
        return productServiceClient.getProductById(id);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        return productServiceClient.updateProduct(id, request);
    }
}
