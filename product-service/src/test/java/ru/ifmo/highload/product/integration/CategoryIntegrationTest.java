package ru.ifmo.highload.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.highload.product.config.TestcontainersConfiguration;
import ru.ifmo.highload.product.dto.category.CategoryCreateRequest;
import ru.ifmo.highload.product.dto.category.CategoryUpdateRequest;
import ru.ifmo.highload.product.security.XUserIdAuthenticationFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CategoryIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_ShouldReturnPaginatedCategories() throws Exception {
        mockMvc.perform(get("/api/category")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }


    @Test
    void createCategory_AsLogistician_ShouldReturn201() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("New Category");
        request.setParentCategoryId(null);

        mockMvc.perform(post("/api/category")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    void createCategory_WithDuplicateName_ShouldReturn400() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Existing Category");
        request.setParentCategoryId(null);

        mockMvc.perform(post("/api/category")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/category")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_ShouldUpdateCategory() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated Category");
        request.setParentCategoryId(null);

        mockMvc.perform(put("/api/category/1")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void updateCategory_WhenNotExists_ShouldReturn404() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("Updated Category");
        request.setParentCategoryId(null);

        mockMvc.perform(put("/api/category/99999")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldReturn204() throws Exception {
        CategoryCreateRequest createRequest = new CategoryCreateRequest();
        createRequest.setName("Category to Delete");
        createRequest.setParentCategoryId(null);

        String response = mockMvc.perform(post("/api/category")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/category/" + categoryId)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/category/99999")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, TEST_USER_ID_HEADER).header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, TEST_LOGISTICIAN_ROLES_HEADER))
                .andExpect(status().isNotFound());
    }
}

