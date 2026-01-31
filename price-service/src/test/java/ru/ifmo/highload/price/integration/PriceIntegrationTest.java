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
import ru.ifmo.highload.price.util.JwtTestHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
class PriceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private DataSource dataSource;

    private String logisticianToken;

    @BeforeEach
    void setUp() {
        setupMockClients();
        logisticianToken = JwtTestHelper.token(JWT_SECRET, 1L, "logistician", "LOGISTICIAN");
    }

    /** Проверка: Liquibase выполнился и в БД есть базовые данные (та же БД, что и для R2DBC). */
    @Test
    void dbShouldHaveBaseDataFromLiquibase() throws Exception {
        assertThat(dataSource).isNotNull();
        try (Connection c = dataSource.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM actual_price")) {
            assertThat(rs.next()).isTrue();
            long count = rs.getLong(1);
            assertThat(count).as("Liquibase должен был вставить 8 строк").isGreaterThanOrEqualTo(8);
        }
    }

    @Test
    void createPrice_ShouldReturn201() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(100L);
        request.setPrice(50000);

        webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.productId").isEqualTo(100)
                .jsonPath("$.price").isEqualTo(50000);
    }

    @Test
    void createPrice_WithNegativePrice_ShouldReturn400() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(100L);
        request.setPrice(-100);

        webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createPrice_WhenPriceAlreadyExists_ShouldReturn400() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(99999);

        webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCurrentPriceForProduct_ShouldReturnPrice() {
        webTestClient.get()
                .uri("/api/price/product/1/current")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo(45000);
    }

    @Test
    void getCurrentPriceForProduct_WhenNotExists_ShouldReturn404() {
        webTestClient.get()
                .uri("/api/price/product/99999/current")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllPrices_ShouldReturnPaginatedPrices() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/price")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").exists();
    }

    @Test
    void updatePrice_ShouldUpdatePrice() throws Exception {
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(200L);
        createRequest.setPrice(30000);

        String response = webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long priceId = objectMapper.readTree(response).get("id").asLong();

        PriceUpdateRequest updateRequest = new PriceUpdateRequest();
        updateRequest.setPrice(35000);

        webTestClient.put()
                .uri("/api/price/" + priceId)
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(priceId)
                .jsonPath("$.price").isEqualTo(35000);
    }

    @Test
    void updatePrice_WhenNotExists_ShouldReturn404() {
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(10000);

        webTestClient.put()
                .uri("/api/price/99999")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePriceByProduct_ShouldUpdatePrice() {
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(400L);
        createRequest.setPrice(30000);

        webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        PriceUpdateRequest updateRequest = new PriceUpdateRequest();
        updateRequest.setPrice(38000);

        webTestClient.put()
                .uri("/api/price/product/400")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.productId").isEqualTo(400)
                .jsonPath("$.price").isEqualTo(38000);
    }

    @Test
    void updatePriceByProduct_WhenNotExists_ShouldReturn404() {
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(10000);

        webTestClient.put()
                .uri("/api/price/product/99999")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePrice_ShouldReturn204() throws Exception {
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(300L);
        createRequest.setPrice(25000);

        String response = webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long priceId = objectMapper.readTree(response).get("id").asLong();

        webTestClient.delete()
                .uri("/api/price/" + priceId)
                .header("Authorization", "Bearer " + logisticianToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePrice_WhenNotExists_ShouldReturn404() {
        webTestClient.delete()
                .uri("/api/price/99999")
                .header("Authorization", "Bearer " + logisticianToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePriceByProduct_ShouldReturn204() {
        PriceCreateRequest createRequest = new PriceCreateRequest();
        createRequest.setProductId(600L);
        createRequest.setPrice(15000);

        webTestClient.post()
                .uri("/api/price")
                .header("Authorization", "Bearer " + logisticianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.delete()
                .uri("/api/price/product/600")
                .header("Authorization", "Bearer " + logisticianToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePriceByProduct_WhenNotExists_StillReturns204() {
        webTestClient.delete()
                .uri("/api/price/product/99999")
                .header("Authorization", "Bearer " + logisticianToken)
                .exchange()
                .expectStatus().isNoContent();
    }
}
