package ru.ifmo.highload.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.highload.product.config.TestcontainersConfiguration;
import ru.ifmo.highload.product.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.product.dto.product.ProductUpdateRequest;
import ru.ifmo.highload.product.util.JwtTestHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProductIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProductsByCategory_ShouldReturnProducts() throws Exception {
        mockMvc.perform(get("/api/product/category/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void getProductById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/product/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/product/search")
                        .param("name", "HQD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllProducts_ShouldReturnPaginatedProducts() throws Exception {
        mockMvc.perform(get("/api/product")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void updateProduct_AsLogistician_ShouldUpdateProduct() throws Exception {
        String token = JwtTestHelper.token(JWT_SECRET, 1L, "logistician", "LOGISTICIAN");
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated Product");
        request.setDescription("Updated Description");
        request.setStockQuantity(100);

        mockMvc.perform(put("/api/product/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    void updateProduct_WhenNotExists_ShouldReturn404() throws Exception {
        String token = JwtTestHelper.token(JWT_SECRET, 1L, "logistician", "LOGISTICIAN");
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated Product");
        request.setDescription("Updated Description");
        request.setStockQuantity(100);

        mockMvc.perform(put("/api/product/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_WithoutAuth_ShouldReturn401() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated");
        request.setDescription("Desc");
        request.setStockQuantity(10);

        mockMvc.perform(put("/api/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

