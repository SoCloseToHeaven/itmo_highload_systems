package ru.ifmo.highload.product.dto.stats;

import lombok.Data;

@Data
public class ProductPurchaseStatsResponse {

    private Long productId;
    private Long totalOrders;
    private Long totalQuantitySold;
}
