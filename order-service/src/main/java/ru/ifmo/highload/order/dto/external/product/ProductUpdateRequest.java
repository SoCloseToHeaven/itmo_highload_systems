package ru.ifmo.highload.order.dto.external.product;

import lombok.Data;

/** Product update DTO (mirrors product-service). Sync when product-service DTO changes. */
@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Integer stockQuantity;
}

