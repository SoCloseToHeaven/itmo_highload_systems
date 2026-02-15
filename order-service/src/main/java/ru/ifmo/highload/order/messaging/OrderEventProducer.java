package ru.ifmo.highload.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.order.config.RabbitMQConfig;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC_ORDER_EVENTS = "order.events";
    private static final String ROUTING_KEY_ORDER_CREATED = "order.created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;

    public OrderEventProducer(
            @org.springframework.beans.factory.annotation.Autowired(required = false) KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            @org.springframework.beans.factory.annotation.Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        if (kafkaTemplate == null && rabbitTemplate == null) {
            return;
        }
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(TOPIC_ORDER_EVENTS, event.getOrderId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Failed to publish OrderCreatedEvent for order {}: {}", event.getOrderId(), ex.getMessage());
                        } else {
                            log.debug("Published OrderCreatedEvent for order {}", event.getOrderId());
                        }
                    });
            } catch (Exception e) {
                log.warn("Failed to publish OrderCreatedEvent for order {}: {}", event.getOrderId(), e.getMessage());
            }
        }
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, ROUTING_KEY_ORDER_CREATED, event);
                log.debug("Sent OrderCreatedEvent to RabbitMQ for order {}", event.getOrderId());
            } catch (Exception e) {
                log.warn("Failed to send OrderCreatedEvent to RabbitMQ for order {}: {}", event.getOrderId(), e.getMessage());
            }
        }
    }

    public static OrderCreatedEvent from(Long orderId, Long userId, Integer totalSum,
                                        List<OrderProductInfo> items) {
        List<OrderCreatedEvent.OrderItemEvent> eventItems = items.stream()
                .map(i -> new OrderCreatedEvent.OrderItemEvent(i.productId(), i.quantity(), i.purchasePrice()))
                .collect(Collectors.toList());
        return new OrderCreatedEvent(orderId, userId, totalSum, eventItems);
    }

    public record OrderProductInfo(Long productId, Integer quantity, Integer purchasePrice) {}
}
