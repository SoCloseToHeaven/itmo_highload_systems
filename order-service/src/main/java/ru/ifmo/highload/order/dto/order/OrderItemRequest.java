package ru.ifmo.highload.order.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Запрос на добавление товара в заказ")
public class OrderItemRequest {

    @NotNull(message = "ID товара обязателен")
    @Schema(description = "ID товара", example = "1", required = true)
    private Long productId;

    @NotNull(message = "Количество товара обязательно")
    @Positive(message = "Количество товара должно быть положительным")
    @Schema(description = "Количество товара", example = "2", required = true)
    private Integer quantity;
}

