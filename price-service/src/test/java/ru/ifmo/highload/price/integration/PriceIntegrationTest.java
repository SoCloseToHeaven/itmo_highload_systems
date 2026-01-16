package ru.ifmo.highload.price.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.ifmo.highload.price.config.TestcontainersConfiguration;
import ru.ifmo.highload.price.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.price.dto.actual_price.PriceUpdateRequest;

@AutoConfigureWebTestClient
class PriceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        setupMockProductService();
    }

    @Test
    void createPrice_ShouldReturn201() throws Exception {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(45000);

        webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.productId").isEqualTo(1)
                .jsonPath("$.price").isEqualTo(45000);
    }

    @Test
    void createPrice_WhenPriceExists_ShouldReturn400() throws Exception {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(45000);

        // First creation should succeed
        webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        // Second creation should fail
        webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCurrentPriceForProduct_ShouldReturnPrice() throws Exception {
        // First create a price
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(2L);
        createRequest.setPrice(50000);

        webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        // Then get it
        webTestClient.get()
                .uri("/api/price/product/2/current")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(50000);
    }

    @Test
    void getCurrentPriceForProduct_WhenNotExists_ShouldReturn404() throws Exception {
        webTestClient.get()
                .uri("/api/price/product/99999/current")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePrice_ShouldUpdatePrice() throws Exception {
        // First create a price
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(3L);
        createRequest.setPrice(45000);

        String response = webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long priceId = objectMapper.readTree(response).get("id").asLong();

        // Then update it
        PriceUpdateRequest updateRequest = new PriceUpdateRequest();
        updateRequest.setPrice(50000);

        webTestClient.put()
                .uri("/api/price/" + priceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price").isEqualTo(50000);
    }

    @Test
    void updatePrice_WhenNotExists_ShouldReturn404() throws Exception {
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(50000);

        webTestClient.put()
                .uri("/api/price/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePriceByProductId_ShouldUpdatePrice() throws Exception {
        // First create a price
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(4L);
        createRequest.setPrice(45000);

        webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        // Then update it
        PriceUpdateRequest updateRequest = new PriceUpdateRequest();
        updateRequest.setPrice(55000);

        webTestClient.put()
                .uri("/api/price/product/4")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price").isEqualTo(55000);
    }

    @Test
    void deletePrice_ShouldReturn204() throws Exception {
        // First create a price
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(5L);
        createRequest.setPrice(45000);

        String response = webTestClient.post()
                .uri("/api/price")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long priceId = objectMapper.readTree(response).get("id").asLong();

        // Then delete it
        webTestClient.delete()
                .uri("/api/price/" + priceId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePrice_WhenNotExists_ShouldReturn404() throws Exception {
        webTestClient.delete()
                .uri("/api/price/99999")
                .exchange()
                .expectStatus().isNotFound();
    }
}

