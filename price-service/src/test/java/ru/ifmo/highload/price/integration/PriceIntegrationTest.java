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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.ifmo.highload.price.security.XUserIdWebFilter.HEADER_X_USER_ID;
import static ru.ifmo.highload.price.security.XUserIdWebFilter.HEADER_X_USER_ROLES;

@AutoConfigureWebTestClient
class PriceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        setupMockClients();
    }

    private WebTestClient.RequestHeadersSpec<?> withAuth(WebTestClient.RequestHeadersSpec<?> spec) {
        return spec.header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER);
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

    /** Проверка: Liquibase создал таблицу истории и применил все changeset'ы (инициализация + базовые данные). */
    @Test
    void liquibaseShouldHaveRunAllChangesets() throws Exception {
        assertThat(dataSource).isNotNull();
        try (Connection c = dataSource.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM databasechangelog")) {
            assertThat(rs.next()).isTrue();
            long count = rs.getLong(1);
            assertThat(count).as("Liquibase должен был выполнить оба changeset'а (1.0.0 + 1.0.1)").isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void createPrice_ShouldReturn201() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(100L);
        request.setPrice(50000);

        webTestClient.post()
                .uri("/api/price")
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        PriceUpdateRequest updateRequest = new PriceUpdateRequest();
        updateRequest.setPrice(38000);

        webTestClient.put()
                .uri("/api/price/product/400")
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePrice_WhenNotExists_ShouldReturn404() {
        webTestClient.delete()
                .uri("/api/price/99999")
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
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
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.delete()
                .uri("/api/price/product/600")
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePriceByProduct_WhenNotExists_StillReturns204() {
        webTestClient.delete()
                .uri("/api/price/product/99999")
                .header(HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(HEADER_X_USER_ROLES, TEST_USER_ROLES_HEADER)
                .exchange()
                .expectStatus().isNoContent();
    }
}
