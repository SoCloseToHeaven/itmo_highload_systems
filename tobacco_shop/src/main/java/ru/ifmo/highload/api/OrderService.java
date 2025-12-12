package ru.ifmo.highload.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.dto.order.OrderCreateRequest;
import ru.ifmo.highload.dto.order.OrderResponse;
import ru.ifmo.highload.dto.order.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse updateOrderStatus(Long id, OrderStatus status);

    Page<OrderResponse> getUserOrders(Long userId, Pageable pageable);

    Page<OrderResponse> getMyOrders(Pageable pageable);

    Page<OrderResponse> getAllOrders(Pageable pageable);
}