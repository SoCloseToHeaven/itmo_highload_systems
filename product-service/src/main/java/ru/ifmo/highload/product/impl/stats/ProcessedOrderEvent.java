package ru.ifmo.highload.product.impl.stats;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "processed_order_events")
@Getter
@Setter
class ProcessedOrderEvent {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "processed_at", nullable = false)
    private ZonedDateTime processedAt;
}
