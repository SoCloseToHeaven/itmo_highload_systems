package ru.ifmo.highload.product.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.product.impl.stats.ProductPurchaseStatsService;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ProductPurchaseStatsService productPurchaseStatsService;

    @KafkaListener(topics = "order.events", groupId = "product-service", containerFactory = "orderEventKafkaListenerContainerFactory")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Kafka: Received OrderCreatedEvent: orderId={}, userId={}, totalSum={}, itemsCount={}",
                event.getOrderId(), event.getUserId(), event.getTotalSum(),
                event.getItems() != null ? event.getItems().size() : 0);
        productPurchaseStatsService.recordOrder(event);
    }
}
