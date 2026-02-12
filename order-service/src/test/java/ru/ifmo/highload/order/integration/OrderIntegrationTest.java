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
import ru.ifmo.highload.order.security.XUserIdWebFilter;

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
    void createOrder_AsUser_ShouldReturn201() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
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
    void createOrder_WithoutAuth_ShouldReturn4xx() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void createOrder_WithEmptyItems_ShouldReturn400() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of());

        webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
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
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        String response = webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        webTestClient.get()
                .uri("/api/order/" + orderId)
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
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
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        String response = webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        webTestClient.put()
                .uri("/api/order/" + orderId)
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
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
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderStatus.PROCESSING)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrderStatus_ToCancelled_ShouldReturn400() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        String response = webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        webTestClient.put()
                .uri("/api/order/" + orderId)
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_USER_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderStatus.CANCELLED)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getAllOrders_WithAuth_ShouldReturnPaginatedOrders() throws Exception {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/order")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_SUPERVISOR_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_SUPERVISOR_ROLES)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").exists();
    }

    @Test
    void getMyOrders_ReturnsOnlyOrdersForCurrentUser() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, "100").header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, "200").header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/my").queryParam("page", "0").queryParam("size", "10").build())
                .header(XUserIdWebFilter.HEADER_X_USER_ID, "100").header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isEqualTo(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/my").queryParam("page", "0").queryParam("size", "10").build())
                .header(XUserIdWebFilter.HEADER_X_USER_ID, "200").header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void getMyOrders_WithoutAuth_ShouldReturn4xx() throws Exception {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/my").queryParam("page", "0").queryParam("size", "10").build())
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void getUserOrders_AsSupervisor_ReturnsOrdersForThatUser() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .header(XUserIdWebFilter.HEADER_X_USER_ID, "50").header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_USER_ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/user/50").queryParam("page", "0").queryParam("size", "10").build())
                .header(XUserIdWebFilter.HEADER_X_USER_ID, TEST_SUPERVISOR_ID).header(XUserIdWebFilter.HEADER_X_USER_ROLES, TEST_SUPERVISOR_ROLES)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isEqualTo(1);
    }
}

