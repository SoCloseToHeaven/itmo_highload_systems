package ru.ifmo.highload.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.ifmo.highload.order.config.TestcontainersConfiguration;
import ru.ifmo.highload.order.dto.order.OrderCreateRequest;
import ru.ifmo.highload.order.dto.order.OrderItemRequest;
import ru.ifmo.highload.order.dto.order.OrderStatus;

import java.util.List;

@AutoConfigureWebTestClient
class OrderIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        setupMockClients();
    }

    @Test
    void createOrder_ShouldReturn201() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.totalSum").exists();
    }

    @Test
    void createOrder_WithEmptyItems_ShouldReturn400() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of());

        webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createOrder_WithNullItems_ShouldReturn400() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(null);

        webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        // First create an order
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        String response = webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        // Then get it
        webTestClient.get()
                .uri("/api/order/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(orderId)
                .jsonPath("$.status").exists()
                .jsonPath("$.totalSum").exists();
    }

    @Test
    void getOrderById_WhenNotExists_ShouldReturn404() throws Exception {
        webTestClient.get()
                .uri("/api/order/99999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        // First create an order
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        String response = webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        // Then update status
        webTestClient.put()
                .uri("/api/order/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderStatus.PROCESSING)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PROCESSING");
    }

    @Test
    void updateOrderStatus_WhenNotExists_ShouldReturn404() throws Exception {
        webTestClient.put()
                .uri("/api/order/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderStatus.PROCESSING)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrderStatus_ToCancelled_ShouldReturn400() throws Exception {
        // First create an order
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        String response = webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        // Then try to cancel it (should fail)
        webTestClient.put()
                .uri("/api/order/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderStatus.CANCELLED)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAllOrders_ShouldReturnPaginatedOrders() throws Exception {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/order")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").exists();
    }
}

