package ru.ifmo.highload.order.dto.external.price;

import lombok.Data;

@Data
public class PriceResponse {
    private Long id;
    private Long productId;
    private Integer price;
}

