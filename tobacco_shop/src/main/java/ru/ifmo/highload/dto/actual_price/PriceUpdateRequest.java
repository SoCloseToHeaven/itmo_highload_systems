package ru.ifmo.highload.dto.actual_price;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление цены")
public class PriceUpdateRequest {

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @Schema(description = "Цена в копейках", example = "45000", required = true)
    private Integer price;
}
