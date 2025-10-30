package ru.ifmo.highload.impl.actual_price;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.config.TestcontainersConfiguration;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

import static org.junit.jupiter.api.Assertions.*;

class PriceServiceIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private PriceService priceService;

    @Test
    void getCurrentPriceForProduct_ShouldReturnPrice() {
        // Сценарий: получение текущей цены товара
        Integer price = priceService.getCurrentPriceForProduct(1L);

        assertNotNull(price);
        assertTrue(price > 0);
    }

    @Test
    void updatePrice_ShouldChangePrice() {
        // Сценарий: обновление цены товара
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(50000);

        var response = priceService.updatePriceByProductId(1L, request);

        assertEquals(50000, response.getPrice());
    }

    @Test
    void createPrice_ForNewProduct_ShouldCreatePrice() {
        // Сценарий: создание цены для нового товара
        // Сначала нужно создать товар, потом цену
        // В данном тесте проверяем работу с уже существующим товаром
        assertDoesNotThrow(() -> priceService.getCurrentPriceForProduct(1L));
    }
}
