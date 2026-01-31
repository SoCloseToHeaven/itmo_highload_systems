package ru.ifmo.highload.product.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.product.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.product.dto.category.CategoryResponse;
import ru.ifmo.highload.product.dto.category.CategoryUpdateRequest;

public interface CategoryService {
    Page<CategoryResponse> getAllCategories(Pageable pageable);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);

    void deleteCategory(Long id);
}

