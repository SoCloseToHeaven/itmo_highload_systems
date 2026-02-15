package ru.ifmo.highload.discount.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.discount.config.RabbitMQConfig;

@Component
@Slf4j
public class OrderCreatedRabbitConsumer {

    @RabbitListener(queues = RabbitMQConfig.DISCOUNT_ORDER_QUEUE)
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("RabbitMQ: Received OrderCreatedEvent: orderId={}, userId={}, totalSum={}",
                event.getOrderId(), event.getUserId(), event.getTotalSum());
    }
}
