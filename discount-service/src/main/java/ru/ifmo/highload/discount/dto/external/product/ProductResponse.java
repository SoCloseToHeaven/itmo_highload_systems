package ru.ifmo.highload.discount.dto.external.product;

import lombok.Data;

/**
 * DTO скопирован из product-service
 * Источник: product-service/dto/product/ProductResponse.java
 * Версия API: v1
 * 
 * ВАЖНО: При изменении в product-service необходимо синхронизировать этот класс!
 */
@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer stockQuantity;
}

