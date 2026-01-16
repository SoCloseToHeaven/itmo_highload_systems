package ru.ifmo.highload.product.impl.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.product.api.CategoryService;
import ru.ifmo.highload.product.dto.category.CategoryResponse;
import ru.ifmo.highload.product.dto.product.ProductResponse;
import ru.ifmo.highload.product.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.product.impl.exceptions.BadRequestException;
import ru.ifmo.highload.product.impl.exceptions.ResourceNotFoundException;

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

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

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
    void getProductsByCategory_WhenCategoryExists_ShouldReturnProducts() {
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setStockQuantity(10);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);
        categoryResponse.setName("Test Category");

        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
        when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.getProductsByCategory(categoryId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
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
    void getProductsByCategory_WhenCategoryServiceThrowsRuntimeException_ShouldThrowResourceNotFoundException() {
        Long categoryId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new RuntimeException("Service error"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductsByCategory(categoryId, pageable));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setStockQuantity(10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void getProductById_WithCategories_ShouldReturnProductWithCategories() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        ProductCategory pc1 = new ProductCategory();
        pc1.setProductId(productId);
        pc1.setCategoryId(1L);

        ProductCategory pc2 = new ProductCategory();
        pc2.setProductId(productId);
        pc2.setCategoryId(2L);

        CategoryResponse cat1 = new CategoryResponse();
        cat1.setId(1L);
        cat1.setName("Category 1");

        CategoryResponse cat2 = new CategoryResponse();
        cat2.setId(2L);
        cat2.setName("Category 2");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of(pc1, pc2));
        when(categoryService.getCategoryById(1L)).thenReturn(cat1);
        when(categoryService.getCategoryById(2L)).thenReturn(cat2);

        ProductResponse result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(2, result.getCategories().size());
    }

    @Test
    void getProductById_WithNonExistentCategory_ShouldFilterOutNullCategories() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        ProductCategory pc1 = new ProductCategory();
        pc1.setProductId(productId);
        pc1.setCategoryId(1L);

        ProductCategory pc2 = new ProductCategory();
        pc2.setProductId(productId);
        pc2.setCategoryId(999L); // Несуществующая категория

        CategoryResponse cat1 = new CategoryResponse();
        cat1.setId(1L);
        cat1.setName("Category 1");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of(pc1, pc2));
        when(categoryService.getCategoryById(1L)).thenReturn(cat1);
        when(categoryService.getCategoryById(999L)).thenThrow(new RuntimeException("Not found"));

        ProductResponse result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(1, result.getCategories().size());
        assertEquals(1L, result.getCategories().get(0).getId());
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated");
        request.setDescription("Updated Description");
        request.setStockQuantity(20);
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old");
        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Updated");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setStockQuantity(20);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.updateProduct(productId, request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
        assertEquals("Updated", result.getName());
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldThrowException() {
        Long productId = 999L;
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated");

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
        product.setName("Test Product");
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
        product.setName("Test Product");

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);
        categoryResponse.setName("Test Category");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
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

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
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
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.addProductToCategory(productId, categoryId));
    }

    @Test
    void removeProductFromCategory_WhenProductInCategory_ShouldRemoveProductFromCategory() {
        Long productId = 1L;
        Long categoryId = 2L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);

        when(productRepository.existsById(productId)).thenReturn(true);
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
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
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.removeProductFromCategory(productId, categoryId));
    }

    @Test
    void getAllCategories_ShouldReturnAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());
        when(productCategoryRepository.findByProductId(2L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.getAllCategories(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}

