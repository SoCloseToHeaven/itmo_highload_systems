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

        StepVerifier.create(orderService.createOrder(request, 1L))
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

        StepVerifier.create(orderService.createOrder(request, 1L))
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
    void createOrder_WithNullItems_ShouldThrowException() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(null);

        StepVerifier.create(orderService.createOrder(request, 1L))
                .expectError(BadRequestException.class)
                .verify();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithMultipleItems_ShouldCreateOrder() {
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item1, item2));

        ProductResponse product1 = new ProductResponse();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setStockQuantity(10);
        product1.setDescription("Description 1");

        ProductResponse product2 = new ProductResponse();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setStockQuantity(5);
        product2.setDescription("Description 2");

        when(priceServiceClient.getCurrentPriceForProduct(1L)).thenReturn(45000);
        when(priceServiceClient.getCurrentPriceForProduct(2L)).thenReturn(50000);
        when(productServiceClient.getProductById(1L)).thenReturn(product1);
        when(productServiceClient.getProductById(2L)).thenReturn(product2);
        when(productServiceClient.updateProduct(anyLong(), any())).thenReturn(product1, product2);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setTotalSum(0);
        savedOrder.setStatus(OrderStatus.PENDING);

        Order finalOrder = new Order();
        finalOrder.setId(1L);
        finalOrder.setTotalSum(140000); // 45000 * 2 + 50000 * 1
        finalOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder, finalOrder);
        when(orderProductRepository.saveAll(anyList())).thenReturn(List.of());

        StepVerifier.create(orderService.createOrder(request, 1L))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.getId());
                    assertEquals(140000, result.getTotalSum());
                })
                .verifyComplete();
    }

    @Test
    void createOrder_WhenProductNotExists_ShouldThrowException() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(999L);
        item.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        Order initialOrder = new Order();
        initialOrder.setId(1L);
        initialOrder.setTotalSum(0);
        initialOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(initialOrder);
        when(productServiceClient.getProductById(999L))
                .thenThrow(new RuntimeException("Product not found"));

        StepVerifier.create(orderService.createOrder(request, 1L))
                .expectError(RuntimeException.class)
                .verify();

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, never()).saveAll(anyList());
    }

    @Test
    void createOrder_WhenPriceNotExists_ShouldThrowException() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Product");
        product.setStockQuantity(10);

        Order initialOrder = new Order();
        initialOrder.setId(1L);
        initialOrder.setTotalSum(0);
        initialOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(initialOrder);
        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(priceServiceClient.getCurrentPriceForProduct(1L))
                .thenThrow(new RuntimeException("Price not found"));

        StepVerifier.create(orderService.createOrder(request, 1L))
                .expectError(RuntimeException.class)
                .verify();

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, never()).saveAll(anyList());
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(20);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("HQD Crystal Plus");
        product.setStockQuantity(10);

        Order initialOrder = new Order();
        initialOrder.setId(1L);
        initialOrder.setTotalSum(0);
        initialOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(initialOrder);
        when(productServiceClient.getProductById(1L)).thenReturn(product);

        StepVerifier.create(orderService.createOrder(request, 1L))
                .expectError(BadRequestException.class)
                .verify();

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, never()).saveAll(anyList());
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldThrowException() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        StepVerifier.create(orderService.getOrderById(orderId))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_WithMultipleItems_ShouldReturnOrderWithItems() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setTotalSum(140000);
        order.setStatus(OrderStatus.PENDING);

        OrderProduct orderProduct1 = new OrderProduct();
        orderProduct1.setOrderId(orderId);
        orderProduct1.setProductId(1L);
        orderProduct1.setQuantity(2);
        orderProduct1.setPurchasePrice(45000);

        OrderProduct orderProduct2 = new OrderProduct();
        orderProduct2.setOrderId(orderId);
        orderProduct2.setProductId(2L);
        orderProduct2.setQuantity(1);
        orderProduct2.setPurchasePrice(50000);

        ProductResponse product1 = new ProductResponse();
        product1.setId(1L);
        product1.setName("Product 1");

        ProductResponse product2 = new ProductResponse();
        product2.setId(2L);
        product2.setName("Product 2");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderProductRepository.findByOrderId(orderId)).thenReturn(List.of(orderProduct1, orderProduct2));
        when(productServiceClient.getProductById(1L)).thenReturn(product1);
        when(productServiceClient.getProductById(2L)).thenReturn(product2);

        StepVerifier.create(orderService.getOrderById(orderId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(orderId, result.getId());
                    assertEquals(2, result.getItems().size());
                    assertEquals("Product 1", result.getItems().get(0).getProductName());
                    assertEquals("Product 2", result.getItems().get(1).getProductName());
                })
                .verifyComplete();
    }

    @Test
    void updateOrderStatus_WhenOrderNotExists_ShouldThrowException() {
        Long orderId = 999L;
        OrderStatus newStatus = OrderStatus.PROCESSING;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        StepVerifier.create(orderService.updateOrderStatus(orderId, newStatus))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WhenStatusIsCancelled_ShouldThrowException() {
        Long orderId = 1L;
        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        StepVerifier.create(orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED))
                .expectError(BadRequestException.class)
                .verify();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getUserOrders_ShouldReturnPaginatedOrders() {
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

        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);
        when(orderProductRepository.findByOrderId(anyLong())).thenReturn(List.of());

        StepVerifier.create(orderService.getUserOrders(1L, pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(2, result.getContent().size());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    void getUserOrders_WhenNoOrders_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(List.of());

        when(orderRepository.findByUserId(1L, pageable)).thenReturn(emptyPage);

        StepVerifier.create(orderService.getUserOrders(1L, pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.getContent().isEmpty());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    void getMyOrders_ShouldReturnPaginatedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        order.setTotalSum(80000);
        order.setStatus(OrderStatus.PENDING);

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);
        when(orderProductRepository.findByOrderId(anyLong())).thenReturn(List.of());

        StepVerifier.create(orderService.getMyOrders(1L, pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1, result.getContent().size());
                    assertEquals(80000, result.getContent().get(0).getTotalSum());
                })
                .verifyComplete();

        verify(orderRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    void getMyOrders_WhenNoOrders_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(List.of());

        when(orderRepository.findByUserId(1L, pageable)).thenReturn(emptyPage);

        StepVerifier.create(orderService.getMyOrders(1L, pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.getContent().isEmpty());
                })
                .verifyComplete();
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

    @Test
    void getAllOrders_WhenNoOrders_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(List.of());

        when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

        StepVerifier.create(orderService.getAllOrders(pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.getContent().isEmpty());
                })
                .verifyComplete();
    }
}

