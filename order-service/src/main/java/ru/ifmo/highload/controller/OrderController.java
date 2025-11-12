package ru.ifmo.highload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.api.OrderService;
import ru.ifmo.highload.controller.order.OrderApi;
import ru.ifmo.highload.dto.order.OrderCreateRequest;
import ru.ifmo.highload.dto.order.OrderResponse;
import ru.ifmo.highload.dto.order.OrderStatus;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    public Mono<ResponseEntity<OrderResponse>> createOrder(OrderCreateRequest request) {
        return orderService.createOrder(request)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<OrderResponse>> getOrder(Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<OrderResponse>> updateOrder(Long orderId, OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Page<OrderResponse>>> getUserOrders(Long userId, Pageable pageable) {
        return orderService.getUserOrders(userId, pageable)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Page<OrderResponse>>> getMyOrders(Pageable pageable) {
        return orderService.getMyOrders(pageable)
                .map(ResponseEntity::ok);
    }
}
