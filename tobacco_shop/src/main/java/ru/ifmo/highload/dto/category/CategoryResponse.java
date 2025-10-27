package ru.ifmo.highload.dto.category;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentCategoryId;
}
