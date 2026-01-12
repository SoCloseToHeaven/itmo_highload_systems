package ru.ifmo.highload.order.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Запрос на создание заказа")
public class OrderCreateRequest {

    @NotEmpty(message = "Заказ должен содержать хотя бы один товар")
    @Valid
    @Schema(description = "Список товаров в заказе", required = true)
    private List<OrderItemRequest> items;
}

