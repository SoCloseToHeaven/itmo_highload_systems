package ru.ifmo.highload.file.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.highload.file.config.TestcontainersConfiguration;
import ru.ifmo.highload.file.security.XUserIdAuthenticationFilter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class FileIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ru.ifmo.highload.file.client.ProductServiceClient productServiceClient;

    private static final byte[] IMAGE_CONTENT = "fake-image-content".getBytes();

    @BeforeEach
    void setUp() {
        setupMockProductClient();
    }

    @Test
    void uploadProductPhoto_asLogistician_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.filename").value("photo.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));
    }

    @Test
    void uploadProductPhoto_asSupervisor_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void uploadProductPhoto_asUser_shouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadProductPhoto_whenProductNotFound_shouldReturn404() throws Exception {
        when(productServiceClient.getProductById(eq(999L))).thenReturn(null);
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/product/999/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadProductPhoto_withInvalidContentType_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadProductPhoto_withoutAuth_shouldReturn401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/product/1/photo").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadInternalFile_asSupervisor_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "internal.png", "image/png", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/upload")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").isEmpty())
                .andExpect(jsonPath("$.filename").value("internal.png"));
    }

    @Test
    void uploadInternalFile_asLogistician_shouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "internal.png", "image/png", IMAGE_CONTENT);

        mockMvc.perform(multipart("/api/file/upload")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFile_asUser_forProductPhoto_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);
        String response = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/file/" + fileId)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void getFileInfo_asUser_forProductPhoto_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);
        String response = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/file/" + fileId + "/info")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId))
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void getProductPhotos_shouldReturnPaginated() throws Exception {
        mockMvc.perform(get("/api/file/product/1")
                        .param("page", "0")
                        .param("size", "10")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void getAllFiles_asSupervisor_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/file")
                        .param("page", "0")
                        .param("size", "10")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllFiles_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/file")
                        .param("page", "0")
                        .param("size", "10")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFile_asLogistician_forProductPhoto_shouldReturn204() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);
        String response = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/file/" + fileId)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_LOGISTICIAN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/file/" + fileId + "/info")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFile_asUser_shouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", IMAGE_CONTENT);
        String response = mockMvc.perform(multipart("/api/file/product/1/photo")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/file/" + fileId)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFile_asUser_forInternalFile_shouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "internal.png", "image/png", IMAGE_CONTENT);
        String response = mockMvc.perform(multipart("/api/file/upload")
                        .file(file)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long fileId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/file/" + fileId)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFile_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/file/99999")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFileInfo_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/file/99999/info")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, USER_ID)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ROLES, ROLES_SUPERVISOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void contextLoads() {
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }
}
