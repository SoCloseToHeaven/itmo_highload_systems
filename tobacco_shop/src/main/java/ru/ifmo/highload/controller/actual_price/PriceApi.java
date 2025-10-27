package ru.ifmo.highload.controller.actual_price;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

@Tag(name = "Управление ценами", description = "API для управления ценами товаров (для администраторов)")
public interface PriceApi {

    @Operation(summary = "Создать цену", description = "Создать новую цену для товара")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Цена успешно создана"),
            @ApiResponse(responseCode = "404", description = "Товар не найден"),
            @ApiResponse(responseCode = "409", description = "Цена для этого товара уже существует")
    })
    @PostMapping
    ResponseEntity<PriceResponse> createPrice(
            @Parameter(description = "Данные для создания цены") @Valid @RequestBody PriceCreateRequest request);

    @Operation(summary = "Обновить цену по ID", description = "Обновить существующую цену по ее ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Цена успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Цена не найдена")
    })
    @PutMapping("/{priceId}")
    ResponseEntity<PriceResponse> updatePrice(
            @Parameter(description = "ID цены для обновления") @PathVariable Long priceId,
            @Parameter(description = "Обновленные данные цены") @Valid @RequestBody PriceUpdateRequest request);

    @Operation(summary = "Удалить цену по ID", description = "Удалить цену по ее ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Цена успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Цена не найдена")
    })
    @DeleteMapping("/{priceId}")
    ResponseEntity<Void> deletePrice(
            @Parameter(description = "ID цены для удаления") @PathVariable Long priceId);

    @Operation(summary = "Обновить цену по ID товара", description = "Обновить цену для конкретного товара")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Цена успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Цена для этого товара не найдена")
    })
    @PutMapping("/product/{productId}")
    ResponseEntity<PriceResponse> updatePriceByProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "Обновленные данные цены") @Valid @RequestBody PriceUpdateRequest request);

    @Operation(summary = "Удалить цену по ID товара", description = "Удалить цену для конкретного товара")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Цена успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Цена для этого товара не найдена")
    })
    @DeleteMapping("/product/{productId}")
    ResponseEntity<Void> deletePriceByProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId);
}
