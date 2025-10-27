package ru.ifmo.highload.dto.product;

import lombok.Data;
import ru.ifmo.highload.dto.category.CategoryResponse;

import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer stockQuantity;
    private Integer currentPrice;
    private List<CategoryResponse> categories;
}
