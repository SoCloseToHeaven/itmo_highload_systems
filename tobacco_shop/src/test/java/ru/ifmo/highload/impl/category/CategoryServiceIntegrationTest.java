package ru.ifmo.highload.impl.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.ifmo.highload.api.CategoryService;
import ru.ifmo.highload.config.TestcontainersConfiguration;
import ru.ifmo.highload.dto.category.CategoryResponse;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private CategoryService categoryService;

    @Test
    void getAllCategories_ShouldReturnPaginatedCategories() {
        // Сценарий: получение всех категорий с пагинацией
        var page = categoryService.getAllCategories(PageRequest.of(0, 5));

        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
        page.getContent().forEach(category -> {
            assertNotNull(category.getId());
            assertNotNull(category.getName());
        });
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        // Сценарий: получение информации о категории
        CategoryResponse category = categoryService.getCategoryById(1L);

        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Табачные изделия", category.getName());
    }

    @Test
    void getRootCategories_ShouldReturnCategoriesWithoutParent() {
        // Сценарий: получение корневых категорий
        var page = categoryService.getRootCategories(PageRequest.of(0, 10));

        assertNotNull(page);
        page.getContent().forEach(category ->
                assertNull(category.getParentCategoryId())
        );
    }
}
