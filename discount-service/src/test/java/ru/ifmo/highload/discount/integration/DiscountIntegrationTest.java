package ru.ifmo.highload.discount.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.highload.discount.config.TestcontainersConfiguration;
import ru.ifmo.highload.discount.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.discount.dto.discount.DiscountUpdateRequest;

import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class DiscountIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        setupMockClients();
    }

    @Test
    void createDiscount_ShouldReturn201() throws Exception {
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));

        mockMvc.perform(post("/api/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.actualPriceId").value(1));
    }

    @Test
    void createDiscount_WithInvalidDateRange_ShouldReturn400() throws Exception {
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(10));
        request.setEndDate(ZonedDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllDiscounts_ShouldReturnPaginatedDiscounts() throws Exception {
        mockMvc.perform(get("/api/discount")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void getActiveDiscounts_ShouldReturnActiveDiscounts() throws Exception {
        // First create an active discount
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().minusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));

        mockMvc.perform(post("/api/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then get active discounts
        mockMvc.perform(get("/api/discount/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateDiscount_ShouldUpdateDiscount() throws Exception {
        // First create a discount
        DiscountCreateRequest createRequest = new DiscountCreateRequest();
        createRequest.setProductId(1L);
        createRequest.setActualPriceId(1L);
        createRequest.setStartDate(ZonedDateTime.now().plusDays(1));
        createRequest.setEndDate(ZonedDateTime.now().plusDays(10));

        String response = mockMvc.perform(post("/api/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long discountId = objectMapper.readTree(response).get("id").asLong();

        // Then update it
        DiscountUpdateRequest updateRequest = new DiscountUpdateRequest();
        updateRequest.setStartDate(ZonedDateTime.now().plusDays(2));
        updateRequest.setEndDate(ZonedDateTime.now().plusDays(12));
        updateRequest.setActualPriceId(1L);

        mockMvc.perform(put("/api/discount/" + discountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(discountId));
    }

    @Test
    void updateDiscount_WhenNotExists_ShouldReturn404() throws Exception {
        DiscountUpdateRequest request = new DiscountUpdateRequest();
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));
        request.setActualPriceId(1L);

        mockMvc.perform(put("/api/discount/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDiscount_ShouldReturn204() throws Exception {
        // First create a discount
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));

        String response = mockMvc.perform(post("/api/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long discountId = objectMapper.readTree(response).get("id").asLong();

        // Then delete it
        mockMvc.perform(delete("/api/discount/" + discountId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDiscount_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/discount/99999"))
                .andExpect(status().isNotFound());
    }
}

