package ru.ifmo.highload.file.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ifmo.highload.file.client.ProductServiceClient;
import ru.ifmo.highload.file.dto.external.ProductResponse;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.bootstrap.enabled=false",
        "spring.cloud.config.enabled=false",
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
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("file.storage-path", () -> Path.of(System.getProperty("java.io.tmpdir"), "file-service-test-" + System.currentTimeMillis()).toString());
    }

    protected static final String HEADER_X_USER_ID = "X-User-Id";
    protected static final String HEADER_X_USER_ROLES = "X-User-Roles";
    protected static final String USER_ID = "1";
    protected static final String ROLES_SUPERVISOR = "SUPERVISOR";
    protected static final String ROLES_LOGISTICIAN = "LOGISTICIAN";
    protected static final String ROLES_USER = "USER";

    protected void setupMockProductClient() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        when(productServiceClient.getProductById(anyLong())).thenReturn(product);
    }
}
