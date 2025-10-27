package ru.ifmo.highload.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;

public interface ProductService {
    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    Page<ProductResponse> searchProducts(String name, Pageable pageable);
    ProductResponse addProductToCategory(Long productId, Long categoryId);
    ProductResponse removeProductFromCategory(Long productId, Long categoryId);
}
