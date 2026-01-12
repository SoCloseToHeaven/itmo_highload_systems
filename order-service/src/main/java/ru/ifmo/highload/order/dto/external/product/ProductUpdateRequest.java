package ru.ifmo.highload.order.dto.external.product;

import lombok.Data;

/**
 * DTO скопирован из product-service
 * Источник: product-service/dto/product/ProductUpdateRequest.java
 * Версия API: v1
 * 
 * ВАЖНО: При изменении в product-service необходимо синхронизировать этот класс!
 */
@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Integer stockQuantity;
}

