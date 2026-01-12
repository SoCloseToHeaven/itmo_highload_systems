package ru.ifmo.highload.discount.dto.external.price;

import lombok.Data;

/**
 * DTO скопирован из price-service
 * Источник: price-service/dto/actual_price/PriceResponse.java
 * Версия API: v1
 * 
 * ВАЖНО: При изменении в price-service необходимо синхронизировать этот класс!
 */
@Data
public class PriceResponse {
    private Long id;
    private Long productId;
    private Integer price;
}

