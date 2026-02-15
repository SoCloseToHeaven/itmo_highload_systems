package ru.ifmo.highload.order.api;

import ru.ifmo.highload.order.dto.external.product.ProductResponse;
import ru.ifmo.highload.order.dto.external.product.ProductUpdateRequest;

public interface ProductDataService {

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
}
