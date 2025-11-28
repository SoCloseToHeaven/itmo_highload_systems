package ru.ifmo.highload.impl.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.CategoryService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryService categoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        try {
            categoryService.getCategoryById(categoryId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найдена категория с id: " + categoryId);
        }

        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден продукт с id: " + id));
        return toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден продукт с id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());

        Product updated = productRepository.save(product);
        return toProductResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toProductResponse);
    }

    @Override
    @Transactional
    public ProductResponse addProductToCategory(Long productId, Long categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден продукт с id: " + productId));

        try {
            categoryService.getCategoryById(categoryId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найдена категория с id: " + categoryId);
        }

        if (productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)) {
            throw new BadRequestException("Продукт уже находится в категории");
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setProductId(productId);
        productCategory.setCategoryId(categoryId);
        productCategoryRepository.save(productCategory);

        return toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse removeProductFromCategory(Long productId, Long categoryId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Не найден продукт с id: " + productId);
        }

        try {
            categoryService.getCategoryById(categoryId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найдена категория с id: " + categoryId);
        }

        productCategoryRepository.deleteByProductIdAndCategoryId(productId, categoryId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден продукт с id: " + productId));
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product product) {
//        Integer currentPrice;
//        try {
//            currentPrice = priceService.getCurrentPriceForProduct(product.getId());
//        } catch (RuntimeException e) {
//            currentPrice = 0; // Если цена не установлена
//        } TODO: Цена продукта должна получаться не здесь, а вызовом отдельного эндпоинта, зная ID продукта

        // Получаем категории продукта через ProductCategoryRepository
        List<ProductCategory> productCategories = productCategoryRepository.findByProductId(product.getId());
        List<CategoryResponse> categories = productCategories.stream()
                .map(pc -> {
                    try {
                        return categoryService.getCategoryById(pc.getCategoryId());
                    } catch (RuntimeException e) {
                        return null; // Пропускаем несуществующие категории
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategories(categories);

        return response;
    }
}