package ru.ifmo.highload.file.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
@AutoConfigureMockMvc
class FileServiceProductIntegrationTest {

    private static WireMockServer wireMockServer;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    private static final String HEADER_X_USER_ID = "X-User-Id";
    private static final String HEADER_X_USER_ROLES = "X-User-Roles";
    private static final String USER_ID = "1";
    private static final String ROLES_LOGISTICIAN = "LOGISTICIAN";

    static {
        postgres.start();
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("file.storage-path", () -> Path.of(System.getProperty("java.io.tmpdir"), "file-product-integration-" + System.currentTimeMillis()).toString());
        registry.add("spring.cloud.openfeign.client.config.product-service.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
    }

    @Test
    void uploadProductPhoto_WhenProductServiceReturnsProduct_ShouldSucceed() throws Exception {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/api/product/1"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(new ObjectMapper().writeValueAsString(Map.of(
                                        "id", 1,
                                        "name", "Product",
                                        "description", "Desc",
                                        "stockQuantity", 10
                                ))))
        );

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-jpeg-content".getBytes());
        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").value(1));

        wireMockServer.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/product/1")));
    }

    @Test
    void uploadProductPhoto_WhenProductServiceReturns404_ShouldReturn404() throws Exception {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/api/product/99999"))
                        .willReturn(WireMock.aResponse().withStatus(404))
        );

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-jpeg-content".getBytes());
        mockMvc.perform(multipart("/api/file/product/99999/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isNotFound());

        wireMockServer.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/product/99999")));
    }

    @Test
    void uploadProductPhoto_UploadedFile_CanBeRetrieved() throws Exception {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/api/product/1"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(new ObjectMapper().writeValueAsString(Map.of(
                                        "id", 1,
                                        "name", "Product",
                                        "description", "Desc",
                                        "stockQuantity", 10
                                ))))
        );

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "integration-test-image".getBytes());
        MvcResult uploadResult = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andReturn();

        long fileId = new ObjectMapper().readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/file/" + fileId)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"));
    }
}
