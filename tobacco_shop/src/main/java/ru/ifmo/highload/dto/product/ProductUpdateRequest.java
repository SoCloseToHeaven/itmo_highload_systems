package ru.ifmo.highload.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление товара")
public class ProductUpdateRequest {

    @NotBlank(message = "Название товара обязательно")
    @Size(max = 255, message = "Название товара не должно превышать 255 символов")
    @Schema(description = "Название товара", example = "HQD Crystal Plus", required = true)
    private String name;

    @Schema(description = "Описание товара", example = "Одноразовая электронная сигарета 2500 тяг")
    private String description;

    @NotNull(message = "Количество товара обязательно")
    @PositiveOrZero(message = "Количество товара должно быть положительным или нулем")
    @Schema(description = "Количество товара на складе", example = "100", required = true)
    private Integer stockQuantity;
}
