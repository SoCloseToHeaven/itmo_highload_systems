package ru.ifmo.highload.discount.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "order.events", groupId = "discount-service", containerFactory = "orderEventKafkaListenerContainerFactory")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Kafka: Received OrderCreatedEvent: orderId={}, userId={}, totalSum={}",
                event.getOrderId(), event.getUserId(), event.getTotalSum());
    }
}
