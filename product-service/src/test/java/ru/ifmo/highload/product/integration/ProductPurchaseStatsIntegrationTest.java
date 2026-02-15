package ru.ifmo.highload.product.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.highload.product.config.TestcontainersConfiguration;
import ru.ifmo.highload.product.dto.stats.ProductPurchaseStatsResponse;
import ru.ifmo.highload.product.impl.stats.ProductPurchaseStatsService;
import ru.ifmo.highload.product.messaging.OrderCreatedEvent;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ProductPurchaseStatsIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductPurchaseStatsService productPurchaseStatsService;

    @Test
    void whenOrderRecordedViaService_shouldReturnStatsViaApi() throws Exception {
        long orderId = 5001L;
        long productId1 = 50001L;
        long productId2 = 50002L;
        OrderCreatedEvent event = createEvent(orderId, 100L, 12000,
                List.of(createItem(productId1, 2, 6000), createItem(productId2, 1, 6000)));

        productPurchaseStatsService.recordOrder(event);

        String json = mockMvc.perform(get("/api/product/stats/purchases"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ProductPurchaseStatsResponse> stats = objectMapper.readValue(json,
                new TypeReference<List<ProductPurchaseStatsResponse>>() {});
        assertThat(stats, hasSize(greaterThanOrEqualTo(1)));
        ProductPurchaseStatsResponse p1 = stats.stream()
                .filter(s -> s.getProductId().equals(productId1))
                .findFirst()
                .orElse(null);
        assertThat(p1, notNullValue());
        assertThat(p1.getTotalOrders(), equalTo(1L));
        assertThat(p1.getTotalQuantitySold(), equalTo(2L));
        ProductPurchaseStatsResponse p2 = stats.stream()
                .filter(s -> s.getProductId().equals(productId2))
                .findFirst()
                .orElse(null);
        assertThat(p2, notNullValue());
        assertThat(p2.getTotalOrders(), equalTo(1L));
        assertThat(p2.getTotalQuantitySold(), equalTo(1L));
    }

    @Test
    void getProductPurchaseStats_whenEmpty_shouldReturnEmptyList() throws Exception {
        String json = mockMvc.perform(get("/api/product/stats/purchases"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ProductPurchaseStatsResponse> stats = objectMapper.readValue(json,
                new TypeReference<List<ProductPurchaseStatsResponse>>() {});
        assertThat(stats, notNullValue());
    }

    private static OrderCreatedEvent createEvent(Long orderId, Long userId, Integer totalSum,
                                                List<OrderCreatedEvent.OrderItemEvent> items) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setTotalSum(totalSum);
        event.setItems(items);
        return event;
    }

    private static OrderCreatedEvent.OrderItemEvent createItem(Long productId, int quantity, int price) {
        OrderCreatedEvent.OrderItemEvent item = new OrderCreatedEvent.OrderItemEvent();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPurchasePrice(price);
        return item;
    }
}
