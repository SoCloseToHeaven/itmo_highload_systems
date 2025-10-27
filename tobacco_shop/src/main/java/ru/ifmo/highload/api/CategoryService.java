package ru.ifmo.highload.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.category.CategoryUpdateRequest;

public interface CategoryService {
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryCreateRequest request);
    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);
    void deleteCategory(Long id);
    Page<CategoryResponse> getRootCategories(Pageable pageable);
}
