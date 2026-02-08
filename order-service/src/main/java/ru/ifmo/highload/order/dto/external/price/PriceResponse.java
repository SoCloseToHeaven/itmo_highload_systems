package ru.ifmo.highload.order.dto.external.price;

import lombok.Data;

/** Price DTO (mirrors price-service). Sync when price-service DTO changes. */
@Data
public class PriceResponse {
    private Long id;
    private Long productId;
    private Integer price;
}

