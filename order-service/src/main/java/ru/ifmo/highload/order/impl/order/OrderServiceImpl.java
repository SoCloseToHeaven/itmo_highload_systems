package ru.ifmo.highload.order.impl.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.order.api.OrderService;
import ru.ifmo.highload.order.client.PriceServiceClient;
import ru.ifmo.highload.order.client.ProductServiceClient;
import ru.ifmo.highload.order.dto.external.product.ProductResponse;
import ru.ifmo.highload.order.dto.external.product.ProductUpdateRequest;
import ru.ifmo.highload.order.dto.order.*;
import ru.ifmo.highload.order.impl.exceptions.BadRequestException;
import ru.ifmo.highload.order.impl.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductServiceClient productServiceClient;
    private final PriceServiceClient priceServiceClient;

    @Override
    public Mono<OrderResponse> createOrder(OrderCreateRequest request) {
        return Mono.fromCallable(() -> createOrderBlocking(request))
                .flatMap(Mono::just);
    }

    @Transactional
    private OrderResponse createOrderBlocking(OrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("В заказе должен быть хотя бы один продукт");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalSum(0);

        Order savedOrder = orderRepository.save(order);

        List<OrderProduct> orderProducts = new ArrayList<>();
        int totalSum = 0;

        for (OrderItemRequest item : request.getItems()) {
            ProductResponse productResponse = productServiceClient.getProductById(item.getProductId());

            if (productResponse.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Недостаточный сток для продукта: " + productResponse.getName());
            }

            Integer currentPrice = priceServiceClient.getCurrentPriceForProduct(item.getProductId());
            int itemTotal = currentPrice * item.getQuantity();
            totalSum += itemTotal;

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrderId(savedOrder.getId());
            orderProduct.setProductId(item.getProductId());
            orderProduct.setQuantity(item.getQuantity());
            orderProduct.setPurchasePrice(currentPrice);

            orderProducts.add(orderProduct);

            // Update stock through product service
            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName(productResponse.getName());
            updateRequest.setDescription(productResponse.getDescription());
            updateRequest.setStockQuantity(productResponse.getStockQuantity() - item.getQuantity());

            productServiceClient.updateProduct(item.getProductId(), updateRequest);
        }

        orderProductRepository.saveAll(orderProducts);

        savedOrder.setTotalSum(totalSum);
        Order finalOrder = orderRepository.save(savedOrder);

        return toOrderResponse(finalOrder);
    }

    @Override
    public Mono<OrderResponse> getOrderById(Long id) {
        return Mono.fromCallable(() -> {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Не найден заказ с id: " + id));
            return toOrderResponse(order);
        });
    }

    @Override
    public Mono<OrderResponse> updateOrderStatus(Long id, OrderStatus status) {
        return Mono.fromCallable(() -> updateOrderStatusBlocking(id, status));
    }

    @Transactional
    private OrderResponse updateOrderStatusBlocking(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден заказ с id: " + id));

        if (status == OrderStatus.CANCELLED) {
            throw new BadRequestException("Заказ не может быть отменен в текущем статусе: " + order.getStatus());
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
    }

    /**
     * Get paginated list of orders for a specific user.
     * Currently returns all orders (user filtering will be implemented in future)
     */
    @Override
    public Mono<Page<OrderResponse>> getUserOrders(Long userId, Pageable pageable) {
        return Mono.fromCallable(() -> {
            Page<Order> orders = orderRepository.findAll(pageable);
            return orders.map(this::toOrderResponse);
        });
    }

    /**
     * Get paginated list of orders for the current user.
     * Currently returns all orders (user filtering will be implemented in future)
     */
    @Override
    public Mono<Page<OrderResponse>> getMyOrders(Pageable pageable) {
        return Mono.fromCallable(() -> {
            Page<Order> orders = orderRepository.findAll(pageable);
            return orders.map(this::toOrderResponse);
        });
    }

    @Override
    public Mono<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        return Mono.fromCallable(() -> {
            Page<Order> orders = orderRepository.findAll(pageable);
            return orders.map(this::toOrderResponse);
        });
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());

        List<OrderItemResponse> items = orderProducts.stream()
                .map(op -> {
                    ProductResponse product = productServiceClient.getProductById(op.getProductId());

                    OrderItemResponse itemResponse = new OrderItemResponse();
                    itemResponse.setProductId(op.getProductId());
                    itemResponse.setProductName(product.getName());
                    itemResponse.setQuantity(op.getQuantity());
                    itemResponse.setPurchasePrice(op.getPurchasePrice());

                    return itemResponse;
                })
                .collect(Collectors.toList());

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalSum(order.getTotalSum());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(items);

        return response;
    }
}

