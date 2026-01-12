package ru.ifmo.highload.order.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Schema(description = "Дата создания заказа", example = "2025-12-28T03:35:07.528+03:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime createdAt;

    @Schema(description = "Список товаров в заказе")
    private List<OrderItemResponse> items;
}

