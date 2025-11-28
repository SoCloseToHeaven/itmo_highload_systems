package ru.ifmo.highload.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Schema(description = "Ответ с информацией о заказе")
public class OrderResponse {

    @Schema(description = "ID заказа", example = "1")
    private Long id;

    @Schema(description = "Общая сумма заказа в копейках", example = "80000")
    private Integer totalSum;

    @Schema(description = "Статус заказа", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Дата создания заказа", example = "2024-01-01T12:00:00")
    private ZonedDateTime createdAt;

    @Schema(description = "Список товаров в заказе")
    private List<OrderItemResponse> items;
}
