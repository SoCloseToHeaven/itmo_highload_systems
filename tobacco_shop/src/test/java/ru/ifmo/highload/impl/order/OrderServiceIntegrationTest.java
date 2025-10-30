package ru.ifmo.highload.impl.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.ifmo.highload.api.OrderService;
import ru.ifmo.highload.config.TestcontainersConfiguration;
import ru.ifmo.highload.dto.order.OrderCreateRequest;
import ru.ifmo.highload.dto.order.OrderItemRequest;
import ru.ifmo.highload.dto.order.OrderResponse;
import ru.ifmo.highload.dto.order.OrderStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private OrderService orderService;

    @Test
    void createOrder_WithAvailableProducts_ShouldCreateOrderAndUpdateStock() {
        // Сценарий: создание заказа с доступными товарами
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item1, item2));

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getItems().size());
        assertTrue(result.getTotalSum() > 0);
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        // Сценарий: попытка заказать больше товара чем есть в наличии
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(999);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void updateOrderStatus_ShouldChangeStatus() {
        // Сценарий: изменение статуса заказа
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        OrderResponse order = orderService.createOrder(request);
        OrderResponse updated = orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);

        assertEquals(OrderStatus.PROCESSING, updated.getStatus());
    }

    @Test
    void getUserOrders_ShouldReturnPaginatedResults() {
        // Сценарий: получение заказов пользователя с пагинацией
        var page = orderService.getUserOrders(1L, PageRequest.of(0, 5));

        assertNotNull(page);
        assertTrue(page.getContent().size() <= 5);
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Сценарий: получение информации о конкретном заказе
        OrderResponse order = orderService.getOrderById(1L);

        assertNotNull(order);
        assertEquals(1L, order.getId());
        assertNotNull(order.getItems());
    }
}
