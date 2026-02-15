package ru.ifmo.highload.product.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreatedEvent {

    private Long orderId;
    private Long userId;
    private Integer totalSum;
    private List<OrderItemEvent> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemEvent {
        private Long productId;
        private Integer quantity;
        private Integer purchasePrice;
    }
}
