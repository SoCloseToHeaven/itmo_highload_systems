package ru.ifmo.highload.order.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ifmo.highload.order.client.PriceServiceClient;
import ru.ifmo.highload.order.client.ProductServiceClient;
import ru.ifmo.highload.order.dto.external.product.ProductResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.bootstrap.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.uri=",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
    }
)
@Testcontainers
public abstract class TestcontainersConfiguration {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @MockBean
    protected ProductServiceClient productServiceClient;

    @MockBean
    protected PriceServiceClient priceServiceClient;

    static {
        postgres.start();
    }

    protected static final String JWT_SECRET = "test-jwt-secret-key-min-32-characters";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("jwt.secret", () -> JWT_SECRET);
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.cloud.bootstrap.enabled", () -> "false");
        registry.add("spring.cloud.config.uri", () -> "");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");
    }

    protected void setupMockClients() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        product.setStockQuantity(100);
        product.setDescription("Test Description");
        when(productServiceClient.getProductById(anyLong())).thenReturn(product);
        when(productServiceClient.updateProduct(anyLong(), any())).thenReturn(product);

        when(priceServiceClient.getCurrentPriceForProduct(anyLong())).thenReturn(45000);
    }
}

