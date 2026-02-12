package ru.ifmo.highload.discount.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ifmo.highload.discount.client.PriceServiceClient;
import ru.ifmo.highload.discount.client.ProductServiceClient;
import ru.ifmo.highload.discount.dto.external.price.PriceResponse;
import ru.ifmo.highload.discount.dto.external.product.ProductResponse;

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

    protected static final String TEST_USER_ID_HEADER = "1";
    protected static final String TEST_SUPERVISOR_ROLES_HEADER = "SUPERVISOR";

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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        
        // Disable Spring Cloud Config
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.cloud.bootstrap.enabled", () -> "false");
        registry.add("spring.cloud.config.uri", () -> "");
        
        // Disable Eureka
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");
    }

    protected void setupMockClients() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        when(productServiceClient.getProductById(anyLong())).thenReturn(product);

        PriceResponse price = new PriceResponse();
        price.setId(1L);
        price.setProductId(1L);
        price.setPrice(45000);
        when(priceServiceClient.getPriceById(anyLong())).thenReturn(price);
    }
}

