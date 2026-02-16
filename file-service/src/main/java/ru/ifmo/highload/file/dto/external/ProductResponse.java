package ru.ifmo.highload.file.dto.external;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer stockQuantity;
}
