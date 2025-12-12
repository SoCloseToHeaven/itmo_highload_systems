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

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
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
    void categoryEntity_onCreate_SetsTimestamps() throws Exception {
        Category category = new Category();
        assertNull(category.getCreatedAt());
        assertNull(category.getUpdatedAt());

        Method onCreate = Category.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(category);

        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
    }

    @Test
    void categoryEntity_onUpdate_SetsUpdatedAt() throws Exception {
        Category category = new Category();
        category.setCreatedAt(ZonedDateTime.now().minusDays(1));

        Method onUpdate = Category.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(category);

        assertNotNull(category.getUpdatedAt());
        assertTrue(category.getUpdatedAt().isAfter(category.getCreatedAt()));
    }

    @Test
    void getAllCategories_WhenCategoriesExist_ShouldReturnPaginatedCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setId(1L);
        category.setName("Test");
        Page<Category> categoryPage = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Test");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getCategoryById_WhenCategoryNotExists_ShouldThrowException() {
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(categoryId));

        assertEquals("Не найдена категория с id: 999", exception.getMessage());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void createCategory_WithValidData_ShouldCreateAndReturnCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("New");
        request.setParentCategoryId(null);

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("New");

        when(categoryRepository.existsByNameAndParentCategoryId("New", null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowException() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Existing");
        request.setParentCategoryId(null);

        when(categoryRepository.existsByNameAndParentCategoryId("Existing", null)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Категория с этим именем уже существует у родителя", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithNonExistentParent_ShouldThrowException() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Child");
        request.setParentCategoryId(999L);

        when(categoryRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Не найдена родительская категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithParentExists_ShouldCreateCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Child");
        request.setParentCategoryId(1L);

        Category savedCategory = new Category();
        savedCategory.setId(2L);
        savedCategory.setName("Child");

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsByNameAndParentCategoryId("Child", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Child", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryExists_ShouldUpdateAndReturnCategory() {
        Long categoryId = 1L;
        Long parentCategoryId = 2L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated");
        request.setParentCategoryId(parentCategoryId);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old");

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Updated");

        Category parentCategory = new Category();
        parentCategory.setId(parentCategoryId);
        parentCategory.setParentCategoryId(null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsById(parentCategoryId)).thenReturn(true);
        when(categoryRepository.findById(parentCategoryId)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByNameAndParentCategoryId("Updated", parentCategoryId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Updated", result.getName());
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenParentIdIsNull_ShouldUpdateCategory() {
        Long categoryId = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated");
        request.setParentCategoryId(null);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old");
        existingCategory.setParentCategoryId(2L);

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Updated");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameAndParentCategoryId("Updated", null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Updated", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenParentIdSameAsCurrent_ShouldNotCheckCircular() {
        Long categoryId = 1L;
        Long parentCategoryId = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated");
        request.setParentCategoryId(parentCategoryId);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old");
        existingCategory.setParentCategoryId(null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsById(parentCategoryId)).thenReturn(true);
        when(categoryRepository.findById(parentCategoryId)).thenReturn(Optional.of(existingCategory));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Циклическая зависимость недопустима", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCyclicalDependency_ShouldThrowException() {
        Long categoryId1 = 1L;
        Long categoryId2 = 2L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated");
        request.setParentCategoryId(categoryId2);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId1);
        existingCategory.setName("Old");
        existingCategory.setParentCategoryId(null);

        Category parentCategory = new Category();
        parentCategory.setId(categoryId2);
        parentCategory.setParentCategoryId(categoryId1);

        when(categoryRepository.findById(categoryId1)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsById(categoryId2)).thenReturn(true);
        when(categoryRepository.findById(categoryId2)).thenReturn(Optional.of(parentCategory));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.updateCategory(categoryId1, request));

        assertEquals("Циклическая зависимость недопустима", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenParentNotExists_ShouldThrowException() {
        Long categoryId = 1L;
        Long parentCategoryId = 999L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated");
        request.setParentCategoryId(parentCategoryId);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsById(parentCategoryId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Не найдена родительская категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenCategoryNotExists_ShouldThrowException() {
        Long categoryId = 999L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Category");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Не найдена категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_WhenCategoryExistsWithoutChildren_ShouldDeleteCategory() {
        Long categoryId = 1L;

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findByParentCategoryId(categoryId)).thenReturn(List.of());
        doNothing().when(categoryRepository).deleteById(categoryId);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenCategoryHasChildren_ShouldThrowException() {
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
        Long categoryId = 999L;
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(categoryId));

        assertEquals("Не найдена категория с id: 999", exception.getMessage());
        verify(categoryRepository, never()).deleteById(categoryId);
    }

    @Test
    void getRootCategories_ShouldReturnRootCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        Category rootCategory = new Category();
        rootCategory.setId(1L);
        rootCategory.setName("Root");
        Page<Category> rootCategories = new PageImpl<>(List.of(rootCategory));

        when(categoryRepository.findByParentCategoryIdIsNull(pageable)).thenReturn(rootCategories);

        Page<CategoryResponse> result = categoryService.getRootCategories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(categoryRepository).findByParentCategoryIdIsNull(pageable);
    }

    @Test
    void updateCategory_WhenNameNotChanged_ShouldNotCheckDuplicateName() {
        Long categoryId = 1L;
        Long parentCategoryId = 2L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Old"); // same name
        request.setParentCategoryId(parentCategoryId);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old");
        existingCategory.setParentCategoryId(parentCategoryId);

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Old");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        // не вызывается проверка existsByNameAndParentCategoryId, потому что имя не изменилось
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // Не должно быть циклической зависимости, так как parentCategoryId не меняется
        // Но в методе isCircularDependency нужно пройти по цепочке. Создадим родителя, который не является циклом.
        Category parentCategory = new Category();
        parentCategory.setId(parentCategoryId);
        parentCategory.setParentCategoryId(null);

        CategoryResponse result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Old", result.getName());
        // Проверяем, что не было вызова existsByNameAndParentCategoryId, так как имя не изменилось
        verify(categoryRepository, never()).existsByNameAndParentCategoryId(eq("Old"), eq(parentCategoryId));
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_WhenParentIdNotChanged_ShouldNotCheckCircular() {
        Long categoryId = 1L;
        Long parentCategoryId = 2L;
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated");
        request.setParentCategoryId(parentCategoryId);

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old");
        existingCategory.setParentCategoryId(parentCategoryId);

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Updated");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        // existsById не вызывается, потому что parentCategoryId не меняется (см. условие в методе)
        when(categoryRepository.existsByNameAndParentCategoryId("Updated", parentCategoryId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse result = categoryService.updateCategory(categoryId, request);

        assertNotNull(result);
        assertEquals("Updated", result.getName());
        // existsById не должен вызываться, так как parentCategoryId не меняется (сравнивается в методе)
        verify(categoryRepository, never()).existsById(anyLong());
        verify(categoryRepository).save(any(Category.class));
    }
}