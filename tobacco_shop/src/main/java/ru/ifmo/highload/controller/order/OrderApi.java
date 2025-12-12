package ru.ifmo.highload.controller.order;

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
import ru.ifmo.highload.dto.order.OrderCreateRequest;
import ru.ifmo.highload.dto.order.OrderResponse;
import ru.ifmo.highload.dto.order.OrderStatus;

@Tag(name = "Управление заказами", description = "API для управления заказами")
public interface OrderApi {

    @Operation(summary = "Создать заказ", description = "Создать новый заказ с поддержкой транзакций")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "400", description = "Неверные данные заказа или недостаточно товара"),
            @ApiResponse(responseCode = "404", description = "Товар не найден")
    })
    @PostMapping
    ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Данные для создания заказа") @Valid @RequestBody OrderCreateRequest request);

    @Operation(summary = "Получить заказ по ID", description = "Получить детальную информацию о заказе по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно получен"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "ID заказа") @PathVariable Long orderId);

    @Operation(summary = "Обновить статус заказа", description = "Обновить статус заказа (отмена, завершение и т.д.) с поддержкой транзакций")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заказа успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден"),
            @ApiResponse(responseCode = "400", description = "Неверный переход статуса")
    })
    @PutMapping("/{orderId}")
    ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "ID заказа для обновления") @PathVariable Long orderId,
            @Parameter(description = "Новый статус заказа") @RequestBody OrderStatus status);

    @Operation(summary = "Получить заказы пользователя", description = "Получить paginated список заказов для конкретного пользователя (для администраторов/поддержки)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказы пользователя успешно получены")
    })
    @GetMapping("/user/{userId}")
    ResponseEntity<Page<OrderResponse>> getUserOrders(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Параметры пагинации", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);

    @Operation(summary = "Получить мои заказы", description = "Получить paginated список заказов текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказы пользователя успешно получены")
    })
    @GetMapping("/my")
    ResponseEntity<Page<OrderResponse>> getMyOrders(
            @Parameter(description = "Параметры пагинации", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}
