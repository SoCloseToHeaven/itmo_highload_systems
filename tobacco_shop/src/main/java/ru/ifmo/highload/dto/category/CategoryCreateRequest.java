package ru.ifmo.highload.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание категории")
public class CategoryCreateRequest {

    @NotBlank(message = "Название категории обязательно")
    @Size(max = 255, message = "Название категории не должно превышать 255 символов")
    @Schema(description = "Название категории", example = "Электронные сигареты", required = true)
    private String name;

    @Schema(description = "ID родительской категории (null для корневых)", example = "1", nullable = true)
    private Long parentCategoryId;
}
