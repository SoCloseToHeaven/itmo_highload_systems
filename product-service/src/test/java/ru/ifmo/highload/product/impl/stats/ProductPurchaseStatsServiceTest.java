package ru.ifmo.highload.product.impl.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.highload.product.dto.stats.ProductPurchaseStatsResponse;
import ru.ifmo.highload.product.messaging.OrderCreatedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPurchaseStatsServiceTest {

    @Mock
    private ProductPurchaseStatsRepository statsRepository;

    @Mock
    private ProcessedOrderEventRepository processedOrderRepository;

    @InjectMocks
    private ProductPurchaseStatsService productPurchaseStatsService;

    @Test
    void recordOrder_WithValidEvent_ShouldUpdateStats() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(1L);
        event.setUserId(100L);
        event.setTotalSum(15000);
        event.setItems(List.of(
                createItem(10L, 2, 5000),
                createItem(20L, 1, 5000)
        ));

        when(processedOrderRepository.existsByOrderId(1L)).thenReturn(false);
        when(statsRepository.findById(10L)).thenReturn(Optional.empty());
        when(statsRepository.findById(20L)).thenReturn(Optional.empty());
        when(statsRepository.save(any(ProductPurchaseStats.class))).thenAnswer(inv -> inv.getArgument(0));

        productPurchaseStatsService.recordOrder(event);

        ArgumentCaptor<ProductPurchaseStats> statsCaptor = ArgumentCaptor.forClass(ProductPurchaseStats.class);
        verify(statsRepository, times(2)).save(statsCaptor.capture());

        List<ProductPurchaseStats> saved = statsCaptor.getAllValues();
        ProductPurchaseStats product10 = saved.stream().filter(s -> s.getProductId() == 10L).findFirst().orElseThrow();
        ProductPurchaseStats product20 = saved.stream().filter(s -> s.getProductId() == 20L).findFirst().orElseThrow();

        assertEquals(1L, product10.getTotalOrders());
        assertEquals(2L, product10.getTotalQuantitySold());
        assertEquals(1L, product20.getTotalOrders());
        assertEquals(1L, product20.getTotalQuantitySold());
        verify(processedOrderRepository).save(any(ProcessedOrderEvent.class));
    }

    @Test
    void recordOrder_WhenOrderAlreadyProcessed_ShouldSkip() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(1L);
        event.setItems(List.of(createItem(10L, 1, 1000)));

        when(processedOrderRepository.existsByOrderId(1L)).thenReturn(true);

        productPurchaseStatsService.recordOrder(event);

        verify(statsRepository, never()).save(any());
        verify(processedOrderRepository, never()).save(any());
    }

    @Test
    void recordOrder_WithNullEvent_ShouldDoNothing() {
        productPurchaseStatsService.recordOrder(null);
        verify(statsRepository, never()).save(any());
        verify(processedOrderRepository, never()).existsByOrderId(anyLong());
    }

    @Test
    void recordOrder_WithNullOrderId_ShouldDoNothing() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(null);
        event.setItems(List.of(createItem(10L, 1, 1000)));

        productPurchaseStatsService.recordOrder(event);
        verify(processedOrderRepository, never()).existsByOrderId(anyLong());
    }

    @Test
    void recordOrder_WithEmptyItems_ShouldStillMarkOrderAsProcessed() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(1L);
        event.setItems(Collections.emptyList());

        when(processedOrderRepository.existsByOrderId(1L)).thenReturn(false);
        when(processedOrderRepository.save(any(ProcessedOrderEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        productPurchaseStatsService.recordOrder(event);

        verify(statsRepository, never()).save(any());
        verify(processedOrderRepository).save(any(ProcessedOrderEvent.class));
    }

    @Test
    void recordOrder_WithExistingStats_ShouldIncrement() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(1L);
        event.setItems(List.of(createItem(10L, 3, 3000)));

        ProductPurchaseStats existing = new ProductPurchaseStats();
        existing.setProductId(10L);
        existing.setTotalOrders(5L);
        existing.setTotalQuantitySold(10L);

        when(processedOrderRepository.existsByOrderId(1L)).thenReturn(false);
        when(statsRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(statsRepository.save(any(ProductPurchaseStats.class))).thenAnswer(inv -> inv.getArgument(0));

        productPurchaseStatsService.recordOrder(event);

        ArgumentCaptor<ProductPurchaseStats> captor = ArgumentCaptor.forClass(ProductPurchaseStats.class);
        verify(statsRepository).save(captor.capture());
        assertEquals(6L, captor.getValue().getTotalOrders());
        assertEquals(13L, captor.getValue().getTotalQuantitySold());
    }

    @Test
    void getAllStats_WhenEmpty_ShouldReturnEmptyList() {
        when(statsRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductPurchaseStatsResponse> result = productPurchaseStatsService.getAllStats();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllStats_WhenStatsExist_ShouldReturnMappedResponses() {
        ProductPurchaseStats s1 = new ProductPurchaseStats();
        s1.setProductId(1L);
        s1.setTotalOrders(10L);
        s1.setTotalQuantitySold(25L);

        when(statsRepository.findAll()).thenReturn(List.of(s1));

        List<ProductPurchaseStatsResponse> result = productPurchaseStatsService.getAllStats();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(10L, result.get(0).getTotalOrders());
        assertEquals(25L, result.get(0).getTotalQuantitySold());
    }

    private static OrderCreatedEvent.OrderItemEvent createItem(Long productId, int quantity, int price) {
        OrderCreatedEvent.OrderItemEvent item = new OrderCreatedEvent.OrderItemEvent();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPurchasePrice(price);
        return item;
    }
}
