package ru.ifmo.highload.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import ru.ifmo.highload.order.dto.order.OrderCreateRequest;
import ru.ifmo.highload.order.dto.order.OrderResponse;
import ru.ifmo.highload.order.dto.order.OrderStatus;

/**
 * Order management API.
 */
@Tag(name = "Order Management", description = "API for managing orders")
public interface OrderApi {

    /** Creates a new order. */
    @Operation(summary = "Create order", description = "Create a new order with transaction support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid order data or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    Mono<ResponseEntity<OrderResponse>> createOrder(
            @Parameter(description = "Order creation data")             @Valid @RequestBody OrderCreateRequest request);

    /** Returns order by ID. */
    @Operation(summary = "Get order by ID", description = "Get detailed order information by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    Mono<ResponseEntity<OrderResponse>> getOrder(
            @Parameter(description = "Order ID")             @PathVariable Long orderId);

    /** Updates order status. */
    @Operation(summary = "Update order status", description = "Update order status (cancellation, completion, etc.) with transaction support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}")
    Mono<ResponseEntity<OrderResponse>> updateOrder(
            @Parameter(description = "Order ID to update") @PathVariable Long orderId,
            @Parameter(description = "New order status")             @RequestBody OrderStatus status);

    /** Returns orders for a user (administrators). */
    @Operation(summary = "Get user orders", description = "Get paginated list of orders for a specific user (for administrators/support)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User orders successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role")
    })
    @GetMapping("/user/{userId}")
    Mono<ResponseEntity<Page<OrderResponse>>> getUserOrders(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    /** Returns orders for the current user. */
    @Operation(summary = "Get my orders", description = "Get paginated list of orders for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User orders successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role")
    })
    @GetMapping("/my")
    Mono<ResponseEntity<Page<OrderResponse>>> getMyOrders(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    /** Returns all orders with pagination. */
    @Operation(summary = "Get all orders", description = "Get paginated list of all orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden – authenticated but insufficient role")
    })
    @GetMapping
    Mono<ResponseEntity<Page<OrderResponse>>> getAllOrders(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""") Pageable pageable);
}

