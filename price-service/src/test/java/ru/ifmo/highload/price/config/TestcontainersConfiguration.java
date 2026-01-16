package ru.ifmo.highload.price.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ifmo.highload.price.client.ProductServiceClient;
import ru.ifmo.highload.price.dto.external.product.ProductResponse;

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

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/testdb",
                postgres.getHost(), postgres.getMappedPort(5432));
        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
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

    protected void setupMockProductService() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        when(productServiceClient.getProductById(anyLong())).thenReturn(product);
    }
}

