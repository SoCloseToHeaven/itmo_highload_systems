package ru.ifmo.highload.product.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "order.events", groupId = "product-service", containerFactory = "orderEventKafkaListenerContainerFactory")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, userId={}, totalSum={}, itemsCount={}",
                event.getOrderId(), event.getUserId(), event.getTotalSum(),
                event.getItems() != null ? event.getItems().size() : 0);
    }
}
