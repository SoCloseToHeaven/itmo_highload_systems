package ru.ifmo.highload.impl.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.CategoryService;
import ru.ifmo.highload.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.category.CategoryUpdateRequest;

@Service
@RequiredArgsConstructor
class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(this::toCategoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (request.getParentCategoryId() != null) {
            if (!categoryRepository.existsById(request.getParentCategoryId())) {
                throw new RuntimeException("Parent category not found with id: " + request.getParentCategoryId());
            }
        }

        // Проверяем уникальность имени в рамках родительской категории
        if (categoryRepository.existsByNameAndParentCategoryId(request.getName(), request.getParentCategoryId())) {
            throw new RuntimeException("Category with this name already exists in the parent category");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setParentCategoryId(request.getParentCategoryId());

        Category saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (request.getParentCategoryId() != null &&
                !request.getParentCategoryId().equals(category.getParentCategoryId())) {
            if (!categoryRepository.existsById(request.getParentCategoryId())) {
                throw new RuntimeException("Parent category not found with id: " + request.getParentCategoryId());
            }

            // Проверяем, не создаем ли циклическую зависимость
            if (isCircularDependency(id, request.getParentCategoryId())) {
                throw new RuntimeException("Circular dependency detected");
            }
        }

        if (!request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndParentCategoryId(request.getName(), request.getParentCategoryId())) {
                throw new RuntimeException("Category with this name already exists in the parent category");
            }
        }

        category.setName(request.getName());
        category.setParentCategoryId(request.getParentCategoryId());

        Category updated = categoryRepository.save(category);
        return toCategoryResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }

        boolean hasChildren = !categoryRepository.findByParentCategoryId(id).isEmpty();
        if (hasChildren) {
            throw new RuntimeException("Cannot delete category with child categories");
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getRootCategories(Pageable pageable) {
        return categoryRepository.findByParentCategoryIdIsNull(pageable)
                .map(this::toCategoryResponse);
    }

    private CategoryResponse toCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setParentCategoryId(category.getParentCategoryId());
        return response;
    }

    private boolean isCircularDependency(Long categoryId, Long potentialParentId) {
        if (categoryId.equals(potentialParentId)) {
            return true;
        }

        Category current = categoryRepository.findById(potentialParentId).orElse(null);
        while (current != null && current.getParentCategoryId() != null) {
            if (current.getParentCategoryId().equals(categoryId)) {
                return true;
            }
            current = categoryRepository.findById(current.getParentCategoryId()).orElse(null);
        }

        return false;
    }
}
