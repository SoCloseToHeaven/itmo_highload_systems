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
import ru.ifmo.highload.order.util.JwtTestHelper;

import java.util.List;

@AutoConfigureWebTestClient
class OrderIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String supervisorToken;

    @BeforeEach
    void setUp() {
        setupMockClients();
        userToken = JwtTestHelper.token(JWT_SECRET, 1L, "user1", "USER");
        supervisorToken = JwtTestHelper.token(JWT_SECRET, 2L, "supervisor", "SUPERVISOR");
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
                .header("Authorization", "Bearer " + userToken)
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
    void createOrder_WithoutAuth_ShouldReturn401() throws Exception {
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
                .expectStatus().isUnauthorized();
    }

    @Test
    void createOrder_WithEmptyItems_ShouldReturn400() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setItems(List.of());

        webTestClient.post()
                .uri("/api/order")
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + userToken)
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
                .header("Authorization", "Bearer " + supervisorToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").exists();
    }

    @Test
    void getMyOrders_ReturnsOnlyOrdersForCurrentUser() throws Exception {
        String tokenUser1 = JwtTestHelper.token(JWT_SECRET, 100L, "user100", "USER");
        String tokenUser2 = JwtTestHelper.token(JWT_SECRET, 200L, "user200", "USER");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .header("Authorization", "Bearer " + tokenUser1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/order")
                .header("Authorization", "Bearer " + tokenUser2)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/my").queryParam("page", "0").queryParam("size", "10").build())
                .header("Authorization", "Bearer " + tokenUser1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isEqualTo(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/my").queryParam("page", "0").queryParam("size", "10").build())
                .header("Authorization", "Bearer " + tokenUser2)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void getMyOrders_WithoutAuth_ShouldReturn401() throws Exception {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/my").queryParam("page", "0").queryParam("size", "10").build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getUserOrders_AsSupervisor_ReturnsOrdersForThatUser() throws Exception {
        String tokenUser = JwtTestHelper.token(JWT_SECRET, 50L, "user50", "USER");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setItems(List.of(item));

        webTestClient.post()
                .uri("/api/order")
                .header("Authorization", "Bearer " + tokenUser)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/order/user/50").queryParam("page", "0").queryParam("size", "10").build())
                .header("Authorization", "Bearer " + supervisorToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isEqualTo(1);
    }
}

