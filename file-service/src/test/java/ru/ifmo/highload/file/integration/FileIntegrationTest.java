package ru.ifmo.highload.file.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.ifmo.highload.file.config.TestcontainersConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class FileIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        setupMockProductClient();
    }

    @Test
    void uploadProductPhoto_AsLogistician_ShouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-content".getBytes());

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.filename").value("photo.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));
    }

    @Test
    void uploadProductPhoto_AsSupervisor_ShouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", "png-data".getBytes());

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void uploadProductPhoto_AsUser_ShouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadInternalFile_AsSupervisor_ShouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "internal.jpg", "image/jpeg", "internal".getBytes());

        mockMvc.perform(multipart("/api/file/upload")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").isEmpty());
    }

    @Test
    void uploadInternalFile_AsLogistician_ShouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "internal.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/api/file/upload")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProductPhotos_AsUser_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/file/product/1")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void getAllFiles_AsSupervisor_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/file")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllFiles_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/file")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFile_ProductPhoto_AsUser_ShouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "photo-content".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andReturn();

        String json = uploadResult.getResponse().getContentAsString();
        long fileId = new ObjectMapper().readTree(json).get("id").asLong();

        mockMvc.perform(get("/api/file/" + fileId)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"));
    }

    @Test
    void deleteProductPhoto_AsLogistician_ShouldReturn204() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "del.jpg", "image/jpeg", "to-delete".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andReturn();

        long fileId = new ObjectMapper().readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/file/" + fileId)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isNoContent());
    }

    @Test
    void getFile_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/file/99999")
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadProductPhoto_WhenProductNotFound_ShouldReturn404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        when(productServiceClient.getProductById(99999L)).thenReturn(null);

        mockMvc.perform(multipart("/api/file/product/99999/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadProductPhoto_WithInvalidContentType_ShouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(HEADER_X_USER_ID, USER_ID)
                        .header(HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestWithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/file/product/1"))
                .andExpect(status().isUnauthorized());
    }
}
