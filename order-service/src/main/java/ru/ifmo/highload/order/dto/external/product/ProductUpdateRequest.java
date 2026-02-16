package ru.ifmo.highload.order.dto.external.product;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Integer stockQuantity;
}

