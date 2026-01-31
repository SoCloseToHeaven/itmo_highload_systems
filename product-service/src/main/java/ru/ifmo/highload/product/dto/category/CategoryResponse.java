package ru.ifmo.highload.product.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с информацией о категории")
public class CategoryResponse {

    @Schema(description = "ID категории", example = "1")
    private Long id;

    @Schema(description = "Название категории", example = "Электронные сигареты")
    private String name;

    @Schema(description = "ID родительской категории (null для корневых)", example = "1", nullable = true)
    private Long parentCategoryId;
}

