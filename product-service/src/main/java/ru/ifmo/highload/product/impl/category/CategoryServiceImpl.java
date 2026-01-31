package ru.ifmo.highload.product.impl.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.product.api.CategoryService;
import ru.ifmo.highload.product.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.product.dto.category.CategoryResponse;
import ru.ifmo.highload.product.dto.category.CategoryUpdateRequest;
import ru.ifmo.highload.product.impl.exceptions.BadRequestException;
import ru.ifmo.highload.product.impl.exceptions.ResourceNotFoundException;

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
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена категория с id: " + id));
        return toCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (request.getParentCategoryId() != null) {
            if (!categoryRepository.existsById(request.getParentCategoryId())) {
                throw new ResourceNotFoundException("Не найдена родительская категория с id: " + request.getParentCategoryId());
            }
        }

        // Check name uniqueness within parent category
        if (categoryRepository.existsByNameAndParentCategoryId(request.getName(), request.getParentCategoryId())) {
            throw new BadRequestException("Категория с этим именем уже существует у родителя");
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
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена категория с id: " + id));

        if (request.getParentCategoryId() != null &&
                !request.getParentCategoryId().equals(category.getParentCategoryId())) {
            if (!categoryRepository.existsById(request.getParentCategoryId())) {
                throw new ResourceNotFoundException("Не найдена родительская категория с id: " + request.getParentCategoryId());
            }

            // Check for circular dependency
            if (isCircularDependency(id, request.getParentCategoryId())) {
                throw new BadRequestException("Циклическая зависимость недопустима");
            }
        }

        if (!request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndParentCategoryId(request.getName(), request.getParentCategoryId())) {
                throw new BadRequestException("Категория с этим именем уже существует у родителя");
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
            throw new ResourceNotFoundException("Не найдена категория с id: " + id);
        }

        boolean hasChildren = !categoryRepository.findByParentCategoryId(id).isEmpty();
        if (hasChildren) {
            throw new BadRequestException("Нельзя удалить категорию с дочерними категориями");
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Convert Category entity to CategoryResponse DTO
     */
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

