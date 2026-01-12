package ru.ifmo.highload.order.impl.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.highload.order.client.PriceServiceClient;
import ru.ifmo.highload.order.client.ProductServiceClient;
import ru.ifmo.highload.order.dto.external.product.ProductResponse;
import ru.ifmo.highload.order.dto.order.*;
import ru.ifmo.highload.order.impl.exceptions.BadRequestException;
import ru.ifmo.highload.order.impl.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private PriceServiceClient priceServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_WithValidItems_ShouldCreateOrder() {
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item1));

        ProductResponse product1 = new ProductResponse();
        product1.setId(1L);
        product1.setName("HQD Crystal Plus");
        product1.setStockQuantity(10);

        when(priceServiceClient.getCurrentPriceForProduct(1L)).thenReturn(45000);
        when(productServiceClient.getProductById(1L)).thenReturn(product1);
        when(productServiceClient.updateProduct(anyLong(), any())).thenReturn(product1);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setTotalSum(0);
        savedOrder.setStatus(OrderStatus.PENDING);

        Order finalOrder = new Order();
        finalOrder.setId(1L);
        finalOrder.setTotalSum(90000);
        finalOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder, finalOrder);
        when(orderProductRepository.saveAll(anyList())).thenReturn(List.of());

        StepVerifier.create(orderService.createOrder(request))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.getId());
                    assertEquals(OrderStatus.PENDING, result.getStatus());
                })
                .verifyComplete();

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createOrder_WithEmptyItems_ShouldThrowException() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of());

        StepVerifier.create(orderService.createOrder(request))
                .expectError(BadRequestException.class)
                .verify();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setTotalSum(80000);
        order.setStatus(OrderStatus.PENDING);

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(orderId);
        orderProduct.setProductId(1L);
        orderProduct.setQuantity(2);
        orderProduct.setPurchasePrice(40000);

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("HQD Crystal Plus");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderProductRepository.findByOrderId(orderId)).thenReturn(List.of(orderProduct));
        when(productServiceClient.getProductById(1L)).thenReturn(product);

        StepVerifier.create(orderService.getOrderById(orderId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(orderId, result.getId());
                    assertEquals(80000, result.getTotalSum());
                    assertEquals(OrderStatus.PENDING, result.getStatus());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateStatus() {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.PROCESSING;

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.PENDING);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(newStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderProductRepository.findByOrderId(orderId)).thenReturn(List.of());

        StepVerifier.create(orderService.updateOrderStatus(orderId, newStatus))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(OrderStatus.PROCESSING, result.getStatus());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getAllOrders_ShouldReturnPaginatedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order1 = new Order();
        order1.setId(1L);
        order1.setTotalSum(80000);
        order1.setStatus(OrderStatus.COMPLETED);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setTotalSum(45000);
        order2.setStatus(OrderStatus.PROCESSING);

        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2));

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderProductRepository.findByOrderId(anyLong())).thenReturn(List.of());

        StepVerifier.create(orderService.getAllOrders(pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(2, result.getContent().size());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findAll(pageable);
    }
}

