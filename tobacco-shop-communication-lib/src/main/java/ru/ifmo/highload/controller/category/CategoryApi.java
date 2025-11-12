package ru.ifmo.highload.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.dto.category.CategoryResponse;
import ru.ifmo.highload.dto.category.CategoryUpdateRequest;

@Tag(name = "Управление категориями", description = "API для управления категориями товаров")
public interface CategoryApi {

    @Operation(summary = "Получить все категории", description = "Получить paginated список всех категорий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категории успешно получены")
    })
    @GetMapping
    ResponseEntity<Page<CategoryResponse>> getCategories(
            @Parameter(description = "Параметры пагинации") Pageable pageable);

    @Operation(summary = "Создать категорию", description = "Создать новую категорию товаров")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные"),
            @ApiResponse(responseCode = "409", description = "Категория с таким именем уже существует")
    })
    @PostMapping
    ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "Данные для создания категории") @Valid @RequestBody CategoryCreateRequest request);

    @Operation(summary = "Обновить категорию", description = "Обновить существующую категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    @PutMapping("/{id}")
    ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID категории для обновления") @PathVariable Long id,
            @Parameter(description = "Обновленные данные категории") @Valid @RequestBody CategoryUpdateRequest request);

    @Operation(summary = "Удалить категорию", description = "Удалить категорию по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить категорию с дочерними категориями")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID категории для удаления") @PathVariable Long id);
}
