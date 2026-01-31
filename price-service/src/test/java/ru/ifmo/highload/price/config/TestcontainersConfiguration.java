package ru.ifmo.highload.price.config;

import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.spi.ConnectionFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ifmo.highload.price.client.ProductServiceClient;
import ru.ifmo.highload.price.dto.external.product.ProductResponse;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.bootstrap.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.uri=",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.liquibase.enabled=true"
    }
)
@Testcontainers
@org.springframework.context.annotation.Import(TestcontainersConfiguration.DataSourceConfig.class)
public abstract class TestcontainersConfiguration {

    @TestConfiguration
    static class DataSourceConfig {
        @Bean
        public DataSource dataSource() {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(postgres.getJdbcUrl());
            ds.setUsername(postgres.getUsername());
            ds.setPassword(postgres.getPassword());
            ds.setDriverClassName("org.postgresql.Driver");
            return ds;
        }

        @Bean
        public SpringLiquibase liquibase(DataSource dataSource) {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
            return liquibase;
        }

        /** Тот же контейнер, что и DataSource — тогда R2DBC видит данные от Liquibase. */
        @Bean
        @Primary
        public ConnectionFactory connectionFactory() {
            String r2dbcUrl = postgres.getJdbcUrl().replace("jdbc:", "r2dbc:");
            return ConnectionFactoryBuilder.withUrl(r2dbcUrl)
                    .username(postgres.getUsername())
                    .password(postgres.getPassword())
                    .build();
        }
    }

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

    protected static final String JWT_SECRET = "test-jwt-secret-key-min-32-characters";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
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
        product.setDescription("Test Description");
        product.setStockQuantity(100);
        lenient().when(productServiceClient.getProductById(any(Long.class))).thenReturn(product);
    }
}
