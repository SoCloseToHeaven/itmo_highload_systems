package ru.ifmo.highload.order.api;

import ru.ifmo.highload.order.model.OrderCreatedEvent;

public interface OrderEventService {

    void publishOrderCreated(OrderCreatedEvent event);
}
