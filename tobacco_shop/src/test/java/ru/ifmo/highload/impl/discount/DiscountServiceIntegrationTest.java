package ru.ifmo.highload.impl.discount;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.DiscountService;
import ru.ifmo.highload.config.TestcontainersConfiguration;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscountServiceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private DiscountService discountService;

    @Test
    @Transactional
    void createDiscount_ShouldCreateAndReturnDiscount() {
        // Сценарий: Создание скидки
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));

        DiscountResponse result = discountService.createDiscount(request);

        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(1L, result.getActualPriceId());
    }

    @Test
    @Transactional
    void updateDiscount_ShouldUpdateAndReturnDiscount() {
        // Сценарий: Обновление скидки
        DiscountCreateRequest createRequest = new DiscountCreateRequest();
        createRequest.setProductId(1L);
        createRequest.setActualPriceId(1L);
        createRequest.setStartDate(ZonedDateTime.now().plusDays(1));
        createRequest.setEndDate(ZonedDateTime.now().plusDays(10));

        DiscountResponse createResult = discountService.createDiscount(createRequest);

        DiscountUpdateRequest updateRequest = new DiscountUpdateRequest();
        updateRequest.setActualPriceId(2L);
        updateRequest.setStartDate(ZonedDateTime.now().plusDays(1));
        updateRequest.setEndDate(ZonedDateTime.now().plusDays(10));

        DiscountResponse updateResult = discountService.updateDiscount(createResult.getId(), updateRequest);

        assertNotNull(updateResult);
        assertEquals(1L, updateResult.getProductId());
        assertEquals(2L, updateResult.getActualPriceId());
    }

    @Test
    @Transactional
    void deleteDiscount_ShouldDeleteDiscount() {
        // Сценарий: Удаление скидки
        DiscountCreateRequest createRequest = new DiscountCreateRequest();
        createRequest.setProductId(1L);
        createRequest.setActualPriceId(1L);
        createRequest.setStartDate(ZonedDateTime.now().minusDays(1));
        createRequest.setEndDate(ZonedDateTime.now().plusDays(10));
        DiscountResponse createResult = discountService.createDiscount(createRequest);

        discountService.deleteDiscount(createResult.getId());

        List<DiscountResponse> getResult = discountService.getActiveDiscounts();

        assertTrue(getResult.isEmpty());
    }

    @Test
    @Transactional
    void getActiveDiscounts_ShouldReturnActiveDiscounts() {
        // Сценарий: Получение активных скидок
        DiscountCreateRequest createRequest = new DiscountCreateRequest();
        createRequest.setProductId(1L);
        createRequest.setActualPriceId(1L);
        createRequest.setStartDate(ZonedDateTime.now().minusDays(1));
        createRequest.setEndDate(ZonedDateTime.now().plusDays(10));
        discountService.createDiscount(createRequest);

        createRequest.setProductId(2L);
        createRequest.setActualPriceId(2L);
        createRequest.setStartDate(ZonedDateTime.now().minusDays(2));
        createRequest.setEndDate(ZonedDateTime.now().plusDays(30));
        discountService.createDiscount(createRequest);

        List<DiscountResponse> getResult = discountService.getActiveDiscounts();

        assertNotNull(getResult);
        assertEquals(2, getResult.size());
        assertTrue(getResult.stream().anyMatch(d -> d.getProductId().equals(1L)));
        assertTrue(getResult.stream().anyMatch(d -> d.getProductId().equals(2L)));
    }

    @Test
    @Transactional
    void getActiveDiscounts_ShouldNotReturnInactiveDiscounts() {
        // Сценарий: Попытка получения неактивных скидок
        DiscountCreateRequest createRequest = new DiscountCreateRequest();
        createRequest.setProductId(1L);
        createRequest.setActualPriceId(1L);
        createRequest.setStartDate(ZonedDateTime.now().minusDays(10));
        createRequest.setEndDate(ZonedDateTime.now().minusDays(1));
        discountService.createDiscount(createRequest);

        createRequest.setProductId(2L);
        createRequest.setActualPriceId(2L);
        createRequest.setStartDate(ZonedDateTime.now().plusDays(2));
        createRequest.setEndDate(ZonedDateTime.now().plusDays(30));
        discountService.createDiscount(createRequest);

        List<DiscountResponse> getResult = discountService.getActiveDiscounts();

        assertTrue(getResult.isEmpty());
    }
}
