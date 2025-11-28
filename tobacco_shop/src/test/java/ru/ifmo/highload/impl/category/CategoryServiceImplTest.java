package ru.ifmo.highload.impl.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.category.CategoryUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getAllCategories_WhenCategoriesExist_ShouldReturnPaginatedCategories() {
        // Сценарий: Получение всех категорий с пагинацией когда категории существуют
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setId(1L);
        category.setName("Электронные сигареты");
        Page<Category> categoryPage = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Электронные сигареты", result.getContent().get(0).getName());
        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() {
        // Сценарий: Получение категории по существующему ID
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Электронные сигареты");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals("Электронные сигареты", result.getName());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void getCategoryById_WhenCategoryNotExists_ShouldThrowException() {
        // Сценарий: Попытка получения несуществующей категории
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(categoryId));

        assertEquals("Не найдена категория с id: 999", exception.getMessage());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void createCategory_WithValidData_ShouldCreateAndReturnCategory() {
        // Сценарий: Создание новой категории с валидными данными
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Новая категория");
        request.setParentCategoryId(null);

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Новая категория");

        when(categoryRepository.existsByNameAndParentCategoryId("Новая категория", null))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Новая категория", result.getName());
        assertNull(result.getParentCategoryId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowException() {
        // Сценарий: Попытка создания категории с уже существующим именем
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Существующая категория");
        request.setParentCategoryId(null);

        when(categoryRepository.existsByNameAndParentCategoryId("Существующая категория", null))
                .thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Категория с этим именем уже существует у родителя", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithNonExistentParent_ShouldThrowException() {
        // Сценарий: Создание категории с несуществующим родителем
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Дочерняя категория");
        request.setParentCategoryId(999L);

        // из-за исключения при проверке existsById
        when(categoryRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Не найдена родительская категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryExists_ShouldUpdateAndReturnCategory() {
        // Сценарий: Обновление существующей категории
        Long categoryId = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Обновленная категория");
        request.setParentCategoryId(null);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Старая категория");

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Обновленная категория");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameAndParentCategoryId("Обновленная категория", null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Обновленная категория", result.getName());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryNotExists_ShouldThrowException() {
        // Сценарий: Попытка обновления несуществующей категории
        Long categoryId = 999L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Категория");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Не найдена категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_WhenCategoryExistsWithoutChildren_ShouldDeleteCategory() {
        // Сценарий: Удаление категории без дочерних категорий
        Long categoryId = 1L;

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findByParentCategoryId(categoryId)).thenReturn(List.of());
        doNothing().when(categoryRepository).deleteById(categoryId);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenCategoryHasChildren_ShouldThrowException() {
        // Сценарий: Попытка удаления категории с дочерними категориями
        Long categoryId = 1L;
        Category childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setParentCategoryId(categoryId);

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findByParentCategoryId(categoryId)).thenReturn(List.of(childCategory));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.deleteCategory(categoryId));

        assertEquals("Нельзя удалить категорию с дочерними категориями", exception.getMessage());
        verify(categoryRepository, never()).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenCategoryNotExists_ShouldThrowException() {
        // Сценарий: Попытка удаления несуществующей категории
        Long categoryId = 999L;
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(categoryId));

        assertEquals("Не найдена категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).deleteById(categoryId);
    }

    @Test
    void getRootCategories_ShouldReturnRootCategories() {
        // Сценарий: Получение корневых категорий (без родителя)
        Pageable pageable = PageRequest.of(0, 10);
        Category rootCategory = new Category();
        rootCategory.setId(1L);
        rootCategory.setName("Корневая категория");
        rootCategory.setParentCategoryId(null);
        Page<Category> rootCategories = new PageImpl<>(List.of(rootCategory));

        when(categoryRepository.findByParentCategoryIdIsNull(pageable)).thenReturn(rootCategories);

        Page<CategoryResponse> result = categoryService.getRootCategories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertNull(result.getContent().get(0).getParentCategoryId());
        verify(categoryRepository, times(1)).findByParentCategoryIdIsNull(pageable);
    }
}