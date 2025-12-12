package ru.ifmo.highload.controller.discount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

import java.util.List;

@Tag(name = "Управление скидками", description = "API для управления скидками на товары (для администраторов)")
public interface DiscountApi {

    @Operation(summary = "Создать скидку", description = "Создать новую скидку для товара")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Скидка успешно создана"),
            @ApiResponse(responseCode = "404", description = "Товар или цена не найдены"),
            @ApiResponse(responseCode = "400", description = "Неверный диапазон дат")
    })
    @PostMapping
    ResponseEntity<DiscountResponse> createDiscount(
            @Parameter(description = "Данные для создания скидки") @Valid @RequestBody DiscountCreateRequest request);

    @Operation(summary = "Обновить скидку", description = "Обновить существующую скидку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Скидка успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Скидка не найдена"),
            @ApiResponse(responseCode = "400", description = "Неверный диапазон дат")
    })
    @PutMapping("/{discountId}")
    ResponseEntity<DiscountResponse> updateDiscount(
            @Parameter(description = "ID скидки для обновления") @PathVariable Long discountId,
            @Parameter(description = "Обновленные данные скидки") @Valid @RequestBody DiscountUpdateRequest request);

    @Operation(summary = "Удалить скидку", description = "Удалить скидку по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Скидка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Скидка не найдена")
    })
    @DeleteMapping("/{discountId}")
    ResponseEntity<Void> deleteDiscount(
            @Parameter(description = "ID скидки для удаления") @PathVariable Long discountId);

    @Operation(summary = "Получить активные скидки", description = "Получить список текущих активных скидок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Активные скидки успешно получены")
    })
    @GetMapping("/active")
    ResponseEntity<List<DiscountResponse>> getActiveDiscounts();

    @Operation(summary = "Получить все скидки", description = "Получить paginated список скидок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Скидки успешно получены")
    })
    @GetMapping
    ResponseEntity<Page<DiscountResponse>> getAllDiscounts(
            @Parameter(description = "Параметры пагинации", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}
