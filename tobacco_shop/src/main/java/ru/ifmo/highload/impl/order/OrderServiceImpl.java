package ru.ifmo.highload.impl.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.OrderService;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.order.*;
import ru.ifmo.highload.dto.product.ProductResponse;
import ru.ifmo.highload.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductService productService;
    private final PriceService priceService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
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
            ProductResponse productResponse = productService.getProductById(item.getProductId());

            if (productResponse.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Недостаточный сток для продукта: " + productResponse.getName());
            }

            Integer currentPrice = priceService.getCurrentPriceForProduct(item.getProductId());
            int itemTotal = currentPrice * item.getQuantity();
            totalSum += itemTotal;

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrderId(savedOrder.getId());
            orderProduct.setProductId(item.getProductId());
            orderProduct.setQuantity(item.getQuantity());
            orderProduct.setPurchasePrice(currentPrice);

            orderProducts.add(orderProduct);

            // Обновляем остаток через сервис продуктов
            ProductUpdateRequest updateRequest =
                    new ProductUpdateRequest();
            updateRequest.setName(productResponse.getName());
            updateRequest.setDescription(productResponse.getDescription());
            updateRequest.setStockQuantity(productResponse.getStockQuantity() - item.getQuantity());

            productService.updateProduct(item.getProductId(), updateRequest);
        }

        orderProductRepository.saveAll(orderProducts);

        savedOrder.setTotalSum(totalSum);
        Order finalOrder = orderRepository.save(savedOrder);

        return toOrderResponse(finalOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден заказ с id: " + id));
        return toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден заказ с id: " + id));

        if (status == OrderStatus.CANCELLED) {
            throw new BadRequestException("Заказ не может быть отменен в текущем статусе: " + order.getStatus());
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        // Временная реализация - возвращаем все заказы
        // В 3-й лабе добавим фильтрацию по пользователю
        return orderRepository.findAll(pageable)
                .map(this::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        // Временная реализация - возвращаем все заказы
        // В 3-й лабе добавим фильтрацию по текущему пользователю
        return orderRepository.findAll(pageable)
                .map(this::toOrderResponse);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());

        List<OrderItemResponse> items = orderProducts.stream()
                .map(op -> {
                    ProductResponse product =
                            productService.getProductById(op.getProductId());

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
