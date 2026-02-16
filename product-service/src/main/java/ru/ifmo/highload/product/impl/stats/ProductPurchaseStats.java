package ru.ifmo.highload.product.impl.stats;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_purchase_stats")
@Getter
@Setter
class ProductPurchaseStats {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "total_orders", nullable = false)
    private Long totalOrders = 0L;

    @Column(name = "total_quantity_sold", nullable = false)
    private Long totalQuantitySold = 0L;
}
