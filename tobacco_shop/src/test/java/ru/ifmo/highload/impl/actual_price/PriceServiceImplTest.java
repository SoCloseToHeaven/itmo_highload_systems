package ru.ifmo.highload.impl.actual_price;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceImplTest {

    @Mock
    private ActualPriceRepository actualPriceRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private PriceServiceImpl priceService;

    @Test
    void createPrice_WhenProductExistsAndPriceNotExists_ShouldCreatePrice() {
        // Сценарий: Создание цены для существующего товара без текущей цены
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(45000);

        ActualPrice savedPrice = new ActualPrice();
        savedPrice.setId(1L);
        savedPrice.setProductId(1L);
        savedPrice.setPrice(45000);

        // Вместо этого мокаем только необходимые вызовы
        when(actualPriceRepository.existsByProductId(1L)).thenReturn(false);
        when(actualPriceRepository.save(any(ActualPrice.class))).thenReturn(savedPrice);

        PriceResponse result = priceService.createPrice(request);

        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(45000, result.getPrice());
        verify(actualPriceRepository, times(1)).save(any(ActualPrice.class));
    }

    @Test
    void createPrice_WhenPriceAlreadyExists_ShouldThrowException() {
        // Сценарий: Попытка создания цены для товара, у которого уже есть цена
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(45000);

        when(actualPriceRepository.existsByProductId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> priceService.createPrice(request));
        verify(actualPriceRepository, never()).save(any(ActualPrice.class));
    }

    @Test
    void updatePrice_WhenPriceExists_ShouldUpdatePrice() {
        // Сценарий: Обновление существующей цены
        Long priceId = 1L;
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(50000);

        ActualPrice existingPrice = new ActualPrice();
        existingPrice.setId(priceId);
        existingPrice.setProductId(1L);
        existingPrice.setPrice(45000);

        ActualPrice updatedPrice = new ActualPrice();
        updatedPrice.setId(priceId);
        updatedPrice.setProductId(1L);
        updatedPrice.setPrice(50000);

        when(actualPriceRepository.findById(priceId)).thenReturn(Optional.of(existingPrice));
        when(actualPriceRepository.save(any(ActualPrice.class))).thenReturn(updatedPrice);

        PriceResponse result = priceService.updatePrice(priceId, request);

        assertNotNull(result);
        assertEquals(50000, result.getPrice());
        verify(actualPriceRepository, times(1)).save(any(ActualPrice.class));
    }

    @Test
    void getCurrentPriceForProduct_WhenPriceExists_ShouldReturnPrice() {
        // Сценарий: Получение текущей цены для товара
        Long productId = 1L;
        ActualPrice price = new ActualPrice();
        price.setId(1L);
        price.setProductId(productId);
        price.setPrice(45000);

        when(actualPriceRepository.findByProductId(productId)).thenReturn(Optional.of(price));

        Integer result = priceService.getCurrentPriceForProduct(productId);

        assertNotNull(result);
        assertEquals(45000, result);
        verify(actualPriceRepository, times(1)).findByProductId(productId);
    }

    @Test
    void getCurrentPriceForProduct_WhenPriceNotExists_ShouldThrowException() {
        // Сценарий: Попытка получения цены для товара без установленной цены
        Long productId = 999L;
        when(actualPriceRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> priceService.getCurrentPriceForProduct(productId));
        verify(actualPriceRepository, times(1)).findByProductId(productId);
    }

    @Test
    void deletePrice_WhenPriceExists_ShouldDeletePrice() {
        // Сценарий: Удаление существующей цены
        Long priceId = 1L;
        when(actualPriceRepository.existsById(priceId)).thenReturn(true);
        doNothing().when(actualPriceRepository).deleteById(priceId);

        priceService.deletePrice(priceId);

        verify(actualPriceRepository, times(1)).deleteById(priceId);
    }

    @Test
    void deletePriceByProductId_ShouldDeletePriceForProduct() {
        // Сценарий: Удаление цены по ID товара
        Long productId = 1L;
        doNothing().when(actualPriceRepository).deleteByProductId(productId);

        priceService.deletePriceByProductId(productId);

        verify(actualPriceRepository, times(1)).deleteByProductId(productId);
    }
}
