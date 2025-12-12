package ru.ifmo.highload.controller.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;

@Tag(name = "Управление товарами", description = "API для управления товарами")
public interface ProductApi {

    @Operation(summary = "Получить товары по категории", description = "Получить paginated список товаров для определенной категории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товары успешно получены"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    @GetMapping("/category/{categoryId}")
    ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @Parameter(description = "ID категории") @PathVariable Long categoryId,
            @Parameter(description = "Параметры пагинации", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);

    @Operation(summary = "Получить товар по ID", description = "Получить детальную информацию о товаре по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно получен"),
            @ApiResponse(responseCode = "404", description = "Товар не найден")
    })
    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID товара") @PathVariable Long id);

    @Operation(summary = "Обновить товар", description = "Обновить информацию о товаре (для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Товар не найден"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    @PutMapping("/{id}")
    ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID товара для обновления") @PathVariable Long id,
            @Parameter(description = "Обновленные данные товара") @RequestBody ProductUpdateRequest request);

    @Operation(summary = "Поиск товаров", description = "Поиск товаров по названию с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товары успешно получены")
    })
    @GetMapping("/search")
    ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Название товара для поиска") @RequestParam String name,
            @Parameter(description = "Параметры пагинации", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}
