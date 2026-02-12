package ru.ifmo.highload.discount.dto.external.product;

import lombok.Data;

/** Product DTO (mirrors product-service). Sync when product-service DTO changes. */
@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer stockQuantity;
}

