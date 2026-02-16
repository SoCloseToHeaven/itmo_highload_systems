package ru.ifmo.highload.product.impl.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.product.dto.stats.ProductPurchaseStatsResponse;
import ru.ifmo.highload.product.messaging.OrderCreatedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPurchaseStatsService {

    private final ProductPurchaseStatsRepository statsRepository;
    private final ProcessedOrderEventRepository processedOrderRepository;

    @Transactional
    public void recordOrder(OrderCreatedEvent event) {
        if (event == null || event.getOrderId() == null) {
            return;
        }
        if (processedOrderRepository.existsByOrderId(event.getOrderId())) {
            log.debug("Order {} already processed, skipping", event.getOrderId());
            return;
        }

        List<OrderCreatedEvent.OrderItemEvent> items = event.getItems();
        if (items == null || items.isEmpty()) {
            processedOrderRepository.save(createProcessedEvent(event.getOrderId()));
            return;
        }

        for (OrderCreatedEvent.OrderItemEvent item : items) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;

            ProductPurchaseStats stats = statsRepository.findById(item.getProductId())
                    .orElseGet(() -> {
                        ProductPurchaseStats newStats = new ProductPurchaseStats();
                        newStats.setProductId(item.getProductId());
                        newStats.setTotalOrders(0L);
                        newStats.setTotalQuantitySold(0L);
                        return newStats;
                    });
            stats.setTotalOrders(stats.getTotalOrders() + 1);
            stats.setTotalQuantitySold(stats.getTotalQuantitySold() + quantity);
            statsRepository.save(stats);
        }

        processedOrderRepository.save(createProcessedEvent(event.getOrderId()));
        log.info("Recorded order {} with {} items", event.getOrderId(), items.size());
    }

    @Transactional(readOnly = true)
    public List<ProductPurchaseStatsResponse> getAllStats() {
        return statsRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductPurchaseStatsResponse toResponse(ProductPurchaseStats stats) {
        ProductPurchaseStatsResponse r = new ProductPurchaseStatsResponse();
        r.setProductId(stats.getProductId());
        r.setTotalOrders(stats.getTotalOrders());
        r.setTotalQuantitySold(stats.getTotalQuantitySold());
        return r;
    }

    private static ProcessedOrderEvent createProcessedEvent(Long orderId) {
        ProcessedOrderEvent e = new ProcessedOrderEvent();
        e.setOrderId(orderId);
        e.setProcessedAt(ZonedDateTime.now());
        return e;
    }
}
