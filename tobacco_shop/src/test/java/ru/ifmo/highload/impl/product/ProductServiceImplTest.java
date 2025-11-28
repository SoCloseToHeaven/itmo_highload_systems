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
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

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

    @Mock
    private PriceService priceService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProductsByCategory_WhenCategoryExists_ShouldReturnProducts() {
        // Сценарий: Получение товаров по существующей категории
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();
        product.setId(1L);
        product.setName("HQD Crystal Plus");
        product.setStockQuantity(10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);
        categoryResponse.setName("Электронные сигареты");

        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
        when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.getProductsByCategory(categoryId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("HQD Crystal Plus", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findByCategoryId(categoryId, pageable);
    }

    @Test
    void getProductsByCategory_WhenCategoryNotExists_ShouldThrowException() {
        // Сценарий: Попытка получения товаров по несуществующей категории
        Long categoryId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductsByCategory(categoryId, pageable));
        verify(productRepository, never()).findByCategoryId(anyLong(), any(Pageable.class));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Сценарий: Получение товара по существующему ID
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("HQD Crystal Plus");
        product.setDescription("Одноразовая электронная сигарета");
        product.setStockQuantity(50);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("HQD Crystal Plus", result.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        // Сценарий: Попытка получения несуществующего товара
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        // Сценарий: Обновление существующего товара
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Обновленный HQD");
        request.setDescription("Новое описание");
        request.setStockQuantity(100);

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Старый HQD");
        existingProduct.setDescription("Старое описание");
        existingProduct.setStockQuantity(50);

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Обновленный HQD");
        updatedProduct.setDescription("Новое описание");
        updatedProduct.setStockQuantity(100);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.updateProduct(productId, request);

        assertNotNull(result);
        assertEquals("Обновленный HQD", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(100, result.getStockQuantity());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        // Сценарий: Поиск товаров по названию
        String searchTerm = "HQD";
        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();
        product.setId(1L);
        product.setName("HQD Crystal Plus");
        product.setStockQuantity(10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByNameContainingIgnoreCase(searchTerm, pageable)).thenReturn(productPage);
        when(productCategoryRepository.findByProductId(1L)).thenReturn(List.of());

        Page<ProductResponse> result = productService.searchProducts(searchTerm, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getName().contains("HQD"));
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(searchTerm, pageable);
    }

    @Test
    void addProductToCategory_WhenProductAndCategoryExist_ShouldAddProductToCategory() {
        // Сценарий: Добавление товара в категорию
        Long productId = 1L;
        Long categoryId = 2L;

        Product product = new Product();
        product.setId(productId);
        product.setName("HQD Crystal Plus");

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);
        categoryResponse.setName("Электронные сигареты");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);
        when(productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)).thenReturn(false);
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(new ProductCategory());

        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.addProductToCategory(productId, categoryId);

        assertNotNull(result);
        verify(productCategoryRepository, times(1)).save(any(ProductCategory.class));
    }

    @Test
    void addProductToCategory_WhenProductAlreadyInCategory_ShouldThrowException() {
        // Сценарий: Попытка добавления товара в категорию, где он уже находится
        Long productId = 1L;
        Long categoryId = 2L;

        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(categoryId)).thenReturn(new CategoryResponse());
        when(productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> productService.addProductToCategory(productId, categoryId));
        verify(productCategoryRepository, never()).save(any(ProductCategory.class));
    }

    @Test
    void removeProductFromCategory_WhenProductInCategory_ShouldRemoveProductFromCategory() {
        // Сценарий: Удаление товара из категории
        Long productId = 1L;
        Long categoryId = 2L;

        Product product = new Product();
        product.setId(productId);
        product.setName("HQD Crystal Plus");

        when(productRepository.existsById(productId)).thenReturn(true);
        when(categoryService.getCategoryById(categoryId)).thenReturn(new CategoryResponse());
        doNothing().when(productCategoryRepository).deleteByProductIdAndCategoryId(productId, categoryId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCategoryRepository.findByProductId(productId)).thenReturn(List.of());

        ProductResponse result = productService.removeProductFromCategory(productId, categoryId);

        assertNotNull(result);
        verify(productCategoryRepository, times(1)).deleteByProductIdAndCategoryId(productId, categoryId);
    }
}
