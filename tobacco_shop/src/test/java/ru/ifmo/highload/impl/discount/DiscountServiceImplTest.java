package ru.ifmo.highload.impl.discount;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private DiscountServiceImpl discountService;

    @Test
    void createDiscount_WithValidData_ShouldCreateAndReturnDiscount() {
        // Сценарий: Создание скидки с валидными данными
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(10));

        Discount savedDiscount = new Discount();
        savedDiscount.setId(1L);
        savedDiscount.setProductId(1L);
        savedDiscount.setActualPriceId(1L);
        savedDiscount.setStartDate(request.getStartDate());
        savedDiscount.setEndDate(request.getEndDate());

        when(productService.getProductById(1L)).thenReturn(null); // Просто проверяем существование
        when(priceService.getCurrentPriceForProduct(1L)).thenReturn(45000);
        when(discountRepository.save(any(Discount.class))).thenReturn(savedDiscount);

        DiscountResponse result = discountService.createDiscount(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getProductId());
        assertEquals(1L, result.getActualPriceId());
        verify(discountRepository, times(1)).save(any(Discount.class));
    }

    @Test
    void createDiscount_WithInvalidDateRange_ShouldThrowException() {
        // Сценарий: Попытка создания скидки с невалидным диапазоном дат
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(10));
        request.setEndDate(LocalDateTime.now().plusDays(1)); // Конечная дата раньше начальной

        when(productService.getProductById(1L)).thenReturn(null);
        when(priceService.getCurrentPriceForProduct(1L)).thenReturn(45000);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> discountService.createDiscount(request));

        assertEquals("Дата конца должна быть позже даты начала", exception.getMessage());
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void createDiscount_WhenProductNotExists_ShouldThrowException() {
        // Сценарий: Попытка создания скидки для несуществующего товара
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(999L);
        request.setActualPriceId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(10));

        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        assertThrows(ResourceNotFoundException.class, () -> discountService.createDiscount(request));
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void updateDiscount_WhenDiscountExists_ShouldUpdateAndReturnDiscount() {
        // Сценарий: Обновление существующей скидки
        Long discountId = 1L;
        DiscountUpdateRequest request = new DiscountUpdateRequest();
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(12));
        request.setActualPriceId(2L);

        Discount existingDiscount = new Discount();
        existingDiscount.setId(discountId);
        existingDiscount.setProductId(1L);
        existingDiscount.setActualPriceId(1L);
        existingDiscount.setStartDate(LocalDateTime.now().plusDays(1));
        existingDiscount.setEndDate(LocalDateTime.now().plusDays(10));

        Discount updatedDiscount = new Discount();
        updatedDiscount.setId(discountId);
        updatedDiscount.setProductId(1L);
        updatedDiscount.setActualPriceId(2L);
        updatedDiscount.setStartDate(request.getStartDate());
        updatedDiscount.setEndDate(request.getEndDate());

        when(discountRepository.findById(discountId)).thenReturn(Optional.of(existingDiscount));
        when(discountRepository.save(any(Discount.class))).thenReturn(updatedDiscount);

        DiscountResponse result = discountService.updateDiscount(discountId, request);

        assertNotNull(result);
        assertEquals(2L, result.getActualPriceId());
        assertEquals(request.getStartDate(), result.getStartDate());
        assertEquals(request.getEndDate(), result.getEndDate());
        verify(discountRepository, times(1)).save(any(Discount.class));
    }

    @Test
    void updateDiscount_WhenDiscountNotExists_ShouldThrowException() {
        // Сценарий: Попытка обновления несуществующей скидки
        Long discountId = 999L;
        DiscountUpdateRequest request = new DiscountUpdateRequest();
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(10));
        request.setActualPriceId(1L);

        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> discountService.updateDiscount(discountId, request));

        assertEquals("Не найдена скидка с id: 999", exception.getMessage());
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void updateDiscount_WithInvalidDateRange_ShouldThrowException() {
        // Сценарий: Попытка обновления скидки с невалидным диапазоном дат
        Long discountId = 1L;
        DiscountUpdateRequest request = new DiscountUpdateRequest();
        request.setStartDate(LocalDateTime.now().plusDays(10));
        request.setEndDate(LocalDateTime.now().plusDays(1)); // Конечная дата раньше начальной
        request.setActualPriceId(1L);

        Discount existingDiscount = new Discount();
        existingDiscount.setId(discountId);

        when(discountRepository.findById(discountId)).thenReturn(Optional.of(existingDiscount));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> discountService.updateDiscount(discountId, request));

        assertEquals("Дата конца должна быть позже даты начала", exception.getMessage());
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void deleteDiscount_WhenDiscountExists_ShouldDeleteDiscount() {
        // Сценарий: Удаление существующей скидки
        Long discountId = 1L;

        when(discountRepository.existsById(discountId)).thenReturn(true);
        doNothing().when(discountRepository).deleteById(discountId);

        discountService.deleteDiscount(discountId);

        verify(discountRepository, times(1)).deleteById(discountId);
    }

    @Test
    void deleteDiscount_WhenDiscountNotExists_ShouldThrowException() {
        // Сценарий: Попытка удаления несуществующей скидки
        Long discountId = 999L;

        when(discountRepository.existsById(discountId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> discountService.deleteDiscount(discountId));

        assertEquals("Не найдена скидка с id: 999", exception.getMessage());
        verify(discountRepository, never()).deleteById(discountId);
    }

    @Test
    void getActiveDiscounts_WhenActiveDiscountsExist_ShouldReturnActiveDiscounts() {
        // Сценарий: Получение активных скидок
        LocalDateTime now = LocalDateTime.now();
        Discount activeDiscount1 = new Discount();
        activeDiscount1.setId(1L);
        activeDiscount1.setProductId(1L);
        activeDiscount1.setActualPriceId(1L);
        activeDiscount1.setStartDate(now.minusDays(1));
        activeDiscount1.setEndDate(now.plusDays(5));

        Discount activeDiscount2 = new Discount();
        activeDiscount2.setId(2L);
        activeDiscount2.setProductId(2L);
        activeDiscount2.setActualPriceId(2L);
        activeDiscount2.setStartDate(now.minusDays(2));
        activeDiscount2.setEndDate(now.plusDays(3));

        List<Discount> activeDiscounts = List.of(activeDiscount1, activeDiscount2);

        when(discountRepository.findByStartDateBeforeAndEndDateAfter(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(activeDiscounts);

        List<DiscountResponse> result = discountService.getActiveDiscounts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(2L)));
        verify(discountRepository, times(1)).findByStartDateBeforeAndEndDateAfter(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getActiveDiscounts_WhenNoActiveDiscounts_ShouldReturnEmptyList() {
        // Сценарий: Получение активных скидок когда их нет
        when(discountRepository.findByStartDateBeforeAndEndDateAfter(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<DiscountResponse> result = discountService.getActiveDiscounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(discountRepository, times(1)).findByStartDateBeforeAndEndDateAfter(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void isActive_WhenDiscountIsActive_ShouldReturnTrue() {
        // Сценарий: Проверка активности скидки (вспомогательный метод)
        Discount discount = new Discount();
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setEndDate(LocalDateTime.now().plusDays(1));

        assertTrue(discount.isActive());
    }

    @Test
    void isActive_WhenDiscountIsNotStarted_ShouldReturnFalse() {
        // Сценарий: Проверка активности скидки которая еще не началась
        Discount discount = new Discount();
        discount.setStartDate(LocalDateTime.now().plusDays(1));
        discount.setEndDate(LocalDateTime.now().plusDays(2));

        assertFalse(discount.isActive());
    }

    @Test
    void isActive_WhenDiscountIsExpired_ShouldReturnFalse() {
        // Сценарий: Проверка активности скидки которая уже истекла
        Discount discount = new Discount();
        discount.setStartDate(LocalDateTime.now().minusDays(2));
        discount.setEndDate(LocalDateTime.now().minusDays(1));

        assertFalse(discount.isActive());
    }
}
