package ru.ifmo.highload.dto.order;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer purchasePrice;
}
