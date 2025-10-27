package ru.ifmo.highload.dto.actual_price;

import lombok.Data;

@Data
public class PriceResponse {
    private Long id;
    private Long productId;
    private Integer price;
}
