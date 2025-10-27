package ru.ifmo.highload.dto.actual_price;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание цены")
public class PriceCreateRequest {

    @NotNull(message = "ID товара обязателен")
    @Schema(description = "ID товара", example = "1", required = true)
    private Long productId;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @Schema(description = "Цена в копейках", example = "45000", required = true)
    private Integer price;
}
