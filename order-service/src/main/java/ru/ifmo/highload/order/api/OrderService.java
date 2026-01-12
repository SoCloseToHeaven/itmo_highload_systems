package ru.ifmo.highload.order.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.order.dto.order.OrderCreateRequest;
import ru.ifmo.highload.order.dto.order.OrderResponse;
import ru.ifmo.highload.order.dto.order.OrderStatus;

public interface OrderService {
    Mono<OrderResponse> createOrder(OrderCreateRequest request);

    Mono<OrderResponse> getOrderById(Long id);

    Mono<OrderResponse> updateOrderStatus(Long id, OrderStatus status);

    Mono<Page<OrderResponse>> getUserOrders(Long userId, Pageable pageable);

    Mono<Page<OrderResponse>> getMyOrders(Pageable pageable);

    Mono<Page<OrderResponse>> getAllOrders(Pageable pageable);
}

