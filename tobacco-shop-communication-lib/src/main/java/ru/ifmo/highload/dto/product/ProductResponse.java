package ru.ifmo.highload.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.ifmo.highload.dto.category.CategoryResponse;

import java.util.List;

@Data
@Schema(description = "Ответ с информацией о товаре")
public class ProductResponse {

    @Schema(description = "ID товара", example = "1")
    private Long id;

    @Schema(description = "Название товара", example = "HQD Crystal Plus")
    private String name;

    @Schema(description = "Описание товара", example = "Одноразовая электронная сигарета 2500 тяг")
    private String description;

    @Schema(description = "Количество товара на складе", example = "100")
    private Integer stockQuantity;

    @Schema(description = "Список категорий товара")
    private List<CategoryResponse> categories;
}
