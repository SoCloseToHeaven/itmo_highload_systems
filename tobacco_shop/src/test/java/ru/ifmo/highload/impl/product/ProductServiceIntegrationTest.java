package ru.ifmo.highload.impl.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.config.TestcontainersConfiguration;
import ru.ifmo.highload.dto.product.ProductResponse;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private ProductService productService;

    @Test
    void getProductsByCategory_ShouldReturnProductsFromCategory() {
        // Сценарий: получение товаров определенной категории
        var page = productService.getProductsByCategory(3L, PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
        page.getContent().forEach(product -> {
            assertNotNull(product.getId());
            assertNotNull(product.getName());
        });
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Сценарий: получение информации о товаре
        ProductResponse product = productService.getProductById(1L);

        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Updated HQD", product.getName());
        assertNotNull(product.getCategories());
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        // Сценарий: поиск товаров по названию
        var page = productService.searchProducts("HQD", PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
        page.getContent().forEach(product ->
                assertTrue(product.getName().contains("HQD"))
        );
    }

    @Test
    void updateProduct_ShouldUpdateProductInfo() {
        // Сценарий: обновление информации о товаре
        var product = productService.getProductById(1L);
        var updateRequest = new ru.ifmo.highload.dto.product.ProductUpdateRequest();
        updateRequest.setName("Updated HQD");
        updateRequest.setDescription(product.getDescription());
        updateRequest.setStockQuantity(product.getStockQuantity());

        ProductResponse updated = productService.updateProduct(1L, updateRequest);

        assertEquals("Updated HQD", updated.getName());
    }
}
