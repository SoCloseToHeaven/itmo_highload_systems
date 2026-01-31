package ru.ifmo.highload.product.controller.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.product.api.CategoryService;
import ru.ifmo.highload.product.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.product.dto.category.CategoryResponse;
import ru.ifmo.highload.product.dto.category.CategoryUpdateRequest;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<Page<CategoryResponse>> getCategories(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public ResponseEntity<CategoryResponse> createCategory(CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public ResponseEntity<CategoryResponse> updateCategory(Long id, CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public ResponseEntity<Void> deleteCategory(Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

