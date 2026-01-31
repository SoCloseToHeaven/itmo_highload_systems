package ru.ifmo.highload.price.dto.actual_price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с информацией о цене")
public class PriceResponse {

    @Schema(description = "ID цены", example = "1")
    private Long id;

    @Schema(description = "ID товара", example = "1")
    private Long productId;

    @Schema(description = "Цена в копейках", example = "45000")
    private Integer price;
}

