package ru.ifmo.highload.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с информацией о товаре в заказе")
public class OrderItemResponse {

    @Schema(description = "ID товара", example = "1")
    private Long productId;

    @Schema(description = "Название товара", example = "HQD Crystal Plus")
    private String productName;

    @Schema(description = "Количество товара", example = "2")
    private Integer quantity;

    @Schema(description = "Цена покупки в копейках", example = "45000")
    private Integer purchasePrice;
}
