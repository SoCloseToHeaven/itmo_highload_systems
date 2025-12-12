package ru.ifmo.highload.impl.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.api.CategoryService;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private CategoryService categoryService;
    @InjectMocks private ProductServiceImpl productService;

    @Test
    void productEntity_onCreate_SetsTimestamps() throws Exception {
        Product product = new Product();
        assertNull(product.getCreatedAt());
        assertNull(product.getUpdatedAt());

        Method onCreate = Product.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(product);

        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    void productEntity_onUpdate_SetsUpdatedAt() throws Exception {
        Product product = new Product();
        product.setCreatedAt(ZonedDateTime.now().minusDays(1));

        Method onUpdate = Product.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(product);

        assertNotNull(product.getUpdatedAt());
        assertTrue(product.getUpdatedAt().isAfter(product.getCreatedAt()));
    }

    @Test
    void productCategoryEntity_ShouldSetAndGetFields() {
        ProductCategory pc = new ProductCategory();
        pc.setId(1L);
        pc.setProductId(100L);
        pc.setCategoryId(200L);

        assertEquals(1L, pc.getId());
        assertEquals(100L, pc.getProductId());
        assertEquals(200L, pc.getCategoryId());
    }

    @Test
    void productRepository_ShouldReturnProductsByCategory() {
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);
        Page<Product> result = productRepository.findByCategoryId(categoryId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void productRepository_ShouldReturnProductsByName() {
        String name = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByNameContainingIgnoreCase(name, pageable)).thenReturn(productPage);
        Page<Product> result = productRepository.findByNameContainingIgnoreCase(name, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void productRepository_ShouldCheckIfExistsByName() {
        String name = "test";

        when(productRepository.existsByName(name)).thenReturn(true);
        boolean exists = productRepository.existsByName(name);

        assertTrue(exists);
    }

    @Test
    void productRepository_ShouldFindAvailableProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(5);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findAvailableProducts(pageable)).thenReturn(productPage);
        Page<Product> result = productRepository.findAvailableProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getProductsByCategory_WhenCategoryExists_ShouldReturnProducts() {
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        CategoryResponse categoryResponse = new CategoryResponse();

        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
        when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.getProductsByCategory(categoryId, pageable);

        assertNotNull(result);
        verify(productRepository).findByCategoryId(categoryId, pageable);
    }

    @Test
    void getProductsByCategory_WhenCategoryNotExists_ShouldThrowException() {
        Long categoryId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductsByCategory(categoryId, pageable));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated");
        Product existingProduct = new Product();
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.updateProduct(productId, request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldThrowException() {
        Long productId = 999L;
        ProductUpdateRequest request = new ProductUpdateRequest();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, request));
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        String searchTerm = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByNameContainingIgnoreCase(searchTerm, pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.searchProducts(searchTerm, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchProducts_WhenNoMatches_ShouldReturnEmptyPage() {
        String searchTerm = "NonExistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());

        when(productRepository.findByNameContainingIgnoreCase(searchTerm, pageable)).thenReturn(emptyPage);

        Page<ProductResponse> result = productService.searchProducts(searchTerm, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void addProductToCategory_WhenProductAndCategoryExist_ShouldAddProductToCategory() {
        Long productId = 1L;
        Long categoryId = 2L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId)).thenReturn(new CategoryResponse());
        when(productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)).thenReturn(false);
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(new ProductCategory());
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.addProductToCategory(productId, categoryId);

        assertNotNull(result);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    void addProductToCategory_WhenProductAlreadyInCategory_ShouldThrowException() {
        Long productId = 1L;
        Long categoryId = 2L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId)).thenReturn(new CategoryResponse());
        when(productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> productService.addProductToCategory(productId, categoryId));
    }

    @Test
    void addProductToCategory_WhenProductNotExists_ShouldThrowException() {
        Long productId = 999L;
        Long categoryId = 2L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.addProductToCategory(productId, categoryId));
    }

    @Test
    void addProductToCategory_WhenCategoryNotExists_ShouldThrowException() {
        Long productId = 1L;
        Long categoryId = 999L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.addProductToCategory(productId, categoryId));
    }

    @Test
    void removeProductFromCategory_WhenProductInCategory_ShouldRemoveProductFromCategory() {
        Long productId = 1L;
        Long categoryId = 2L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.existsById(productId)).thenReturn(true);
        when(categoryService.getCategoryById(categoryId)).thenReturn(new CategoryResponse());
        doNothing().when(productCategoryRepository).deleteByProductIdAndCategoryId(productId, categoryId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.removeProductFromCategory(productId, categoryId);

        assertNotNull(result);
        verify(productCategoryRepository).deleteByProductIdAndCategoryId(productId, categoryId);
    }

    @Test
    void removeProductFromCategory_WhenProductNotExists_ShouldThrowException() {
        Long productId = 999L;
        Long categoryId = 2L;

        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.removeProductFromCategory(productId, categoryId));
    }

    @Test
    void removeProductFromCategory_WhenCategoryNotExists_ShouldThrowException() {
        Long productId = 1L;
        Long categoryId = 999L;

        when(productRepository.existsById(productId)).thenReturn(true);
        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.removeProductFromCategory(productId, categoryId));
    }

    @Test
    void getAllCategories_ShouldReturnAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product1 = new Product();
        product1.setId(1L);
        Product product2 = new Product();
        product2.setId(2L);
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());
        when(productCategoryRepository.findByProductId(2L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.getAllCategories(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}