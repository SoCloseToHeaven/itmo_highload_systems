package ru.ifmo.highload.impl.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.order.OrderCreateRequest;
import ru.ifmo.highload.dto.order.OrderItemRequest;
import ru.ifmo.highload.dto.order.OrderResponse;
import ru.ifmo.highload.dto.order.OrderStatus;
import ru.ifmo.highload.dto.product.ProductResponse;

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
    private ProductService productService;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_WithValidItems_ShouldCreateOrder() {
        // Сценарий: Создание заказа с валидными товарами
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item1));

        // Моки для продуктов
        ProductResponse product1 = new ProductResponse();
        product1.setId(1L);
        product1.setName("HQD Crystal Plus");
        product1.setStockQuantity(10);

        // Моки для цен
        when(priceService.getCurrentPriceForProduct(1L)).thenReturn(45000);

        // Моки для получения продуктов
        when(productService.getProductById(1L)).thenReturn(product1);

        // Моки для сохранения заказа и товаров заказа
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

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createOrder_WithEmptyItems_ShouldThrowException() {
        // Сценарий: Попытка создания заказа без товаров
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request));

        assertEquals("Order must contain at least one item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithNullItems_ShouldThrowException() {
        // Сценарий: Попытка создания заказа с null списком товаров
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request));

        assertEquals("Order must contain at least one item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        // Сценарий: Попытка создания заказа с недостаточным количеством товара
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(20); // Заказываем больше чем есть в наличии

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("HQD Crystal Plus");
        product.setStockQuantity(10); // В наличии только 10

        when(productService.getProductById(1L)).thenReturn(product);

        // Создаем начальный заказ который будет сохранен до проверки запасов
        Order initialOrder = new Order();
        initialOrder.setId(1L);
        initialOrder.setTotalSum(0);
        initialOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(initialOrder);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request));

        assertEquals("Insufficient stock for product: HQD Crystal Plus", exception.getMessage());

        // Проверяем что заказ был создан, но затем произошла ошибка
        verify(orderRepository, times(1)).save(any(Order.class));
        // Проверяем что не было сохранения order products
        verify(orderProductRepository, never()).saveAll(anyList());
    }

    @Test
    void createOrder_WhenProductNotExists_ShouldThrowException() {
        // Сценарий: Попытка создания заказа с несуществующим товаром
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(999L);
        item.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        // Создаем начальный заказ который будет сохранен до проверки товара
        Order initialOrder = new Order();
        initialOrder.setId(1L);
        initialOrder.setTotalSum(0);
        initialOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(initialOrder);
        when(productService.getProductById(999L))
                .thenThrow(new RuntimeException("Product not found"));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));

        // Проверяем что заказ был создан, но затем произошла ошибка
        verify(orderRepository, times(1)).save(any(Order.class));
        // Проверяем что не было сохранения order products
        verify(orderProductRepository, never()).saveAll(anyList());
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Сценарий: Получение заказа по существующему ID
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
        when(productService.getProductById(1L)).thenReturn(product);

        OrderResponse result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(80000, result.getTotalSum());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(1, result.getItems().size());
        assertEquals("HQD Crystal Plus", result.getItems().get(0).getProductName());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldThrowException() {
        // Сценарий: Попытка получения несуществующего заказа
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(orderId));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateStatus() {
        // Сценарий: Обновление статуса существующего заказа
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

        // УБИРАЕМ ненужные заглушки для toOrderResponse
        when(orderProductRepository.findByOrderId(orderId)).thenReturn(List.of());

        OrderResponse result = orderService.updateOrderStatus(orderId, newStatus);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WhenOrderNotExists_ShouldThrowException() {
        // Сценарий: Попытка обновления статуса несуществующего заказа
        Long orderId = 999L;
        OrderStatus newStatus = OrderStatus.PROCESSING;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(orderId, newStatus));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WhenCancellingCompletedOrder_ShouldThrowException() {
        // Сценарий: Обновление статуса завершенного заказа на CANCELLED
        Long orderId = 1L;
        OrderStatus cancelledStatus = OrderStatus.CANCELLED;

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.COMPLETED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(cancelledStatus);

        assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(orderId, cancelledStatus));
    }

    @Test
    void getUserOrders_ShouldReturnPaginatedOrders() {
        // Сценарий: Получение заказов пользователя с пагинацией
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

        // УБИРАЕМ ненужные заглушки для toOrderResponse
        when(orderProductRepository.findByOrderId(anyLong())).thenReturn(List.of());

        Page<OrderResponse> result = orderService.getUserOrders(1L, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void getMyOrders_ShouldReturnPaginatedOrders() {
        // Сценарий: Получение заказов текущего пользователя с пагинацией
        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setId(1L);
        order.setTotalSum(80000);
        order.setStatus(OrderStatus.PENDING);

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // УБИРАЕМ ненужные заглушки для toOrderResponse
        when(orderProductRepository.findByOrderId(anyLong())).thenReturn(List.of());

        Page<OrderResponse> result = orderService.getMyOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(80000, result.getContent().get(0).getTotalSum());
        verify(orderRepository, times(1)).findAll(pageable);
    }
}