package ru.ifmo.highload.discount.impl.discount;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.discount.client.PriceServiceClient;
import ru.ifmo.highload.discount.client.ProductServiceClient;
import ru.ifmo.highload.discount.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.discount.dto.discount.DiscountResponse;
import ru.ifmo.highload.discount.dto.discount.DiscountUpdateRequest;
import ru.ifmo.highload.discount.dto.external.price.PriceResponse;
import ru.ifmo.highload.discount.dto.external.product.ProductResponse;
import ru.ifmo.highload.discount.impl.exceptions.BadRequestException;
import ru.ifmo.highload.discount.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;
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
    private ProductServiceClient productServiceClient;

    @Mock
    private PriceServiceClient priceServiceClient;

    @InjectMocks
    private DiscountServiceImpl discountService;

    @Test
    void createDiscount_WithValidData_ShouldCreateAndReturnDiscount() {
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));

        ProductResponse product = new ProductResponse();
        product.setId(1L);

        PriceResponse price = new PriceResponse();
        price.setId(1L);

        Discount savedDiscount = new Discount();
        savedDiscount.setId(1L);
        savedDiscount.setProductId(1L);
        savedDiscount.setActualPriceId(1L);
        savedDiscount.setStartDate(request.getStartDate());
        savedDiscount.setEndDate(request.getEndDate());

        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(priceServiceClient.getPriceById(1L)).thenReturn(price);
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
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(1L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(10));
        request.setEndDate(ZonedDateTime.now().plusDays(1));

        ProductResponse product = new ProductResponse();
        PriceResponse price = new PriceResponse();

        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(priceServiceClient.getPriceById(1L)).thenReturn(price);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> discountService.createDiscount(request));

        assertEquals("Дата конца должна быть позже даты начала", exception.getMessage());
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void createDiscount_WhenProductNotExists_ShouldThrowException() {
        DiscountCreateRequest request = new DiscountCreateRequest();
        request.setProductId(999L);
        request.setActualPriceId(1L);
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));

        when(productServiceClient.getProductById(999L))
                .thenThrow(new RuntimeException("Product not found"));

        assertThrows(ResourceNotFoundException.class, () -> discountService.createDiscount(request));
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void updateDiscount_WhenDiscountExists_ShouldUpdateAndReturnDiscount() {
        Long discountId = 1L;
        DiscountUpdateRequest request = new DiscountUpdateRequest();
        request.setStartDate(ZonedDateTime.now().plusDays(2));
        request.setEndDate(ZonedDateTime.now().plusDays(12));
        request.setActualPriceId(2L);

        Discount existingDiscount = new Discount();
        existingDiscount.setId(discountId);
        existingDiscount.setProductId(1L);
        existingDiscount.setActualPriceId(1L);

        Discount updatedDiscount = new Discount();
        updatedDiscount.setId(discountId);
        updatedDiscount.setProductId(1L);
        updatedDiscount.setActualPriceId(2L);
        updatedDiscount.setStartDate(request.getStartDate());
        updatedDiscount.setEndDate(request.getEndDate());

        PriceResponse price = new PriceResponse();
        price.setId(2L);

        when(discountRepository.findById(discountId)).thenReturn(Optional.of(existingDiscount));
        when(priceServiceClient.getPriceById(2L)).thenReturn(price);
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
        Long discountId = 999L;
        DiscountUpdateRequest request = new DiscountUpdateRequest();
        request.setStartDate(ZonedDateTime.now().plusDays(1));
        request.setEndDate(ZonedDateTime.now().plusDays(10));
        request.setActualPriceId(1L);

        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> discountService.updateDiscount(discountId, request));

        assertEquals("Не найдена скидка с id: 999", exception.getMessage());
        verify(discountRepository, never()).save(any(Discount.class));
    }

    @Test
    void deleteDiscount_WhenDiscountExists_ShouldDeleteDiscount() {
        Long discountId = 1L;

        when(discountRepository.existsById(discountId)).thenReturn(true);
        doNothing().when(discountRepository).deleteById(discountId);

        discountService.deleteDiscount(discountId);

        verify(discountRepository, times(1)).deleteById(discountId);
    }

    @Test
    void deleteDiscount_WhenDiscountNotExists_ShouldThrowException() {
        Long discountId = 999L;

        when(discountRepository.existsById(discountId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> discountService.deleteDiscount(discountId));

        assertEquals("Не найдена скидка с id: 999", exception.getMessage());
        verify(discountRepository, never()).deleteById(discountId);
    }

    @Test
    void getActiveDiscounts_WhenActiveDiscountsExist_ShouldReturnActiveDiscounts() {
        ZonedDateTime now = ZonedDateTime.now();
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

        when(discountRepository.findByStartDateBeforeAndEndDateAfter(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(activeDiscounts);

        List<DiscountResponse> result = discountService.getActiveDiscounts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(2L)));
        verify(discountRepository, times(1)).findByStartDateBeforeAndEndDateAfter(any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @Test
    void getAllDiscounts_ShouldReturnDiscounts() {
        ZonedDateTime now = ZonedDateTime.now();
        Discount discount1 = new Discount();
        discount1.setId(1L);
        discount1.setProductId(1L);
        discount1.setActualPriceId(1L);
        discount1.setStartDate(now.minusDays(1));
        discount1.setEndDate(now.plusDays(5));

        Discount discount2 = new Discount();
        discount2.setId(2L);
        discount2.setProductId(2L);
        discount2.setActualPriceId(2L);
        discount2.setStartDate(now.minusDays(2));
        discount2.setEndDate(now.plusDays(3));

        Pageable pageable = PageRequest.of(0, 10);
        List<Discount> discounts = List.of(discount1, discount2);

        when(discountRepository.findAll(pageable)).thenReturn(new PageImpl<>(discounts));

        Page<DiscountResponse> result = discountService.getAllDiscounts(pageable);

        assertNotNull(result);
        assertEquals(2, result.getNumberOfElements());
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(d -> d.getId().equals(2L)));
        verify(discountRepository, times(1)).findAll(pageable);
    }
}

