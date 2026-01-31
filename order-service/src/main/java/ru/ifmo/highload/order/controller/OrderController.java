package ru.ifmo.highload.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.order.api.OrderService;
import ru.ifmo.highload.order.dto.order.OrderCreateRequest;
import ru.ifmo.highload.order.dto.order.OrderResponse;
import ru.ifmo.highload.order.dto.order.OrderStatus;
import ru.ifmo.highload.order.security.CurrentUser;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','SUPERVISOR')")
    public Mono<ResponseEntity<OrderResponse>> createOrder(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid OrderCreateRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (CurrentUser) auth.getPrincipal())
                .flatMap(user -> orderService.createOrder(request, user.getUserId())
                        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response)));
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
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (CurrentUser) auth.getPrincipal())
                .flatMap(user -> orderService.getMyOrders(user.getUserId(), pageable)
                        .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Page<OrderResponse>>> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable)
                .map(ResponseEntity::ok);
    }
}

