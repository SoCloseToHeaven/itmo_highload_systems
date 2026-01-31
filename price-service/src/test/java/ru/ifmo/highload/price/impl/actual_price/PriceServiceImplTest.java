package ru.ifmo.highload.price.impl.actual_price;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.ifmo.highload.price.client.ProductServiceClient;
import ru.ifmo.highload.price.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.price.dto.actual_price.PriceResponse;
import ru.ifmo.highload.price.dto.actual_price.PriceUpdateRequest;
import ru.ifmo.highload.price.dto.external.product.ProductResponse;
import ru.ifmo.highload.price.impl.exceptions.BadRequestException;
import ru.ifmo.highload.price.impl.exceptions.ResourceNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceImplTest {

    @Mock
    private ActualPriceRepository actualPriceRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private PriceServiceImpl priceService;

    @Test
    void createPrice_WhenProductExistsAndPriceNotExists_ShouldCreatePrice() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(45000);

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");

        ActualPrice savedPrice = new ActualPrice();
        savedPrice.setId(1L);
        savedPrice.setProductId(1L);
        savedPrice.setPrice(45000);

        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(actualPriceRepository.countByProductId(1L)).thenReturn(Mono.just(0L));
        when(actualPriceRepository.save(any(ActualPrice.class))).thenReturn(Mono.just(savedPrice));

        StepVerifier.create(priceService.createPrice(request))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.getProductId());
                    assertEquals(45000, result.getPrice());
                })
                .verifyComplete();

        verify(actualPriceRepository, times(1)).save(any(ActualPrice.class));
    }

    @Test
    void createPrice_WhenPriceAlreadyExists_ShouldThrowException() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(1L);
        request.setPrice(45000);

        ProductResponse product = new ProductResponse();
        product.setId(1L);

        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(actualPriceRepository.countByProductId(1L)).thenReturn(Mono.just(1L));

        StepVerifier.create(priceService.createPrice(request))
                .expectError(BadRequestException.class)
                .verify();

        verify(actualPriceRepository, never()).save(any(ActualPrice.class));
    }

    @Test
    void updatePrice_WhenPriceExists_ShouldUpdatePrice() {
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

        when(actualPriceRepository.findById(priceId)).thenReturn(Mono.just(existingPrice));
        when(actualPriceRepository.save(any(ActualPrice.class))).thenReturn(Mono.just(updatedPrice));

        StepVerifier.create(priceService.updatePrice(priceId, request))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(50000, result.getPrice());
                })
                .verifyComplete();

        verify(actualPriceRepository, times(1)).save(any(ActualPrice.class));
    }

    @Test
    void getCurrentPriceForProduct_WhenPriceExists_ShouldReturnPrice() {
        Long productId = 1L;
        ActualPrice price = new ActualPrice();
        price.setId(1L);
        price.setProductId(productId);
        price.setPrice(45000);

        when(actualPriceRepository.findByProductId(productId)).thenReturn(Mono.just(price));

        StepVerifier.create(priceService.getCurrentPriceForProduct(productId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(45000, result);
                })
                .verifyComplete();

        verify(actualPriceRepository, times(1)).findByProductId(productId);
    }

    @Test
    void getCurrentPriceForProduct_WhenPriceNotExists_ShouldThrowException() {
        Long productId = 999L;
        when(actualPriceRepository.findByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.getCurrentPriceForProduct(productId))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, times(1)).findByProductId(productId);
    }

    @Test
    void deletePrice_WhenPriceExists_ShouldDeletePrice() {
        Long priceId = 1L;
        ActualPrice price = new ActualPrice();
        price.setId(priceId);
        when(actualPriceRepository.findById(priceId)).thenReturn(Mono.just(price));
        when(actualPriceRepository.deleteById(priceId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.deletePrice(priceId))
                .verifyComplete();

        verify(actualPriceRepository, times(1)).deleteById(priceId);
    }

    @Test
    void deletePriceByProductId_ShouldDeletePriceForProduct() {
        Long productId = 1L;
        when(actualPriceRepository.deleteByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.deletePriceByProductId(productId))
                .verifyComplete();

        verify(actualPriceRepository, times(1)).deleteByProductId(productId);
    }

    @Test
    void createPrice_WhenProductNotExists_ShouldThrowException() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(999L);
        request.setPrice(45000);

        when(productServiceClient.getProductById(999L))
                .thenThrow(new RuntimeException("Product not found"));

        StepVerifier.create(priceService.createPrice(request))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, never()).save(any(ActualPrice.class));
    }

    @Test
    void createPrice_WhenProductServiceThrowsResourceNotFoundException_ShouldPropagateException() {
        PriceCreateRequest request = new PriceCreateRequest();
        request.setProductId(999L);
        request.setPrice(45000);

        when(productServiceClient.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        StepVerifier.create(priceService.createPrice(request))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, never()).save(any(ActualPrice.class));
    }

    @Test
    void updatePrice_WhenPriceNotExists_ShouldThrowException() {
        Long priceId = 999L;
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(50000);

        when(actualPriceRepository.findById(priceId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.updatePrice(priceId, request))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, never()).save(any(ActualPrice.class));
    }

    @Test
    void updatePriceByProductId_WhenPriceExists_ShouldUpdatePrice() {
        Long productId = 1L;
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(50000);

        ActualPrice existingPrice = new ActualPrice();
        existingPrice.setId(1L);
        existingPrice.setProductId(productId);
        existingPrice.setPrice(45000);

        ActualPrice updatedPrice = new ActualPrice();
        updatedPrice.setId(1L);
        updatedPrice.setProductId(productId);
        updatedPrice.setPrice(50000);

        when(actualPriceRepository.findByProductId(productId)).thenReturn(Mono.just(existingPrice));
        when(actualPriceRepository.save(any(ActualPrice.class))).thenReturn(Mono.just(updatedPrice));

        StepVerifier.create(priceService.updatePriceByProductId(productId, request))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(50000, result.getPrice());
                    assertEquals(productId, result.getProductId());
                })
                .verifyComplete();

        verify(actualPriceRepository, times(1)).save(any(ActualPrice.class));
    }

    @Test
    void updatePriceByProductId_WhenPriceNotExists_ShouldThrowException() {
        Long productId = 999L;
        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setPrice(50000);

        when(actualPriceRepository.findByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.updatePriceByProductId(productId, request))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, never()).save(any(ActualPrice.class));
    }

    @Test
    void deletePrice_WhenPriceNotExists_ShouldThrowException() {
        Long priceId = 999L;
        when(actualPriceRepository.findById(priceId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.deletePrice(priceId))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, never()).deleteById(anyLong());
    }

    @Test
    void getPriceById_WhenPriceExists_ShouldReturnPrice() {
        Long priceId = 1L;
        ActualPrice price = new ActualPrice();
        price.setId(priceId);
        price.setProductId(1L);
        price.setPrice(45000);

        when(actualPriceRepository.findById(priceId)).thenReturn(Mono.just(price));

        StepVerifier.create(priceService.getPriceById(priceId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(priceId, result.getId());
                    assertEquals(1L, result.getProductId());
                    assertEquals(45000, result.getPrice());
                })
                .verifyComplete();

        verify(actualPriceRepository, times(1)).findById(priceId);
    }

    @Test
    void getPriceById_WhenPriceNotExists_ShouldThrowException() {
        Long priceId = 999L;
        when(actualPriceRepository.findById(priceId)).thenReturn(Mono.empty());

        StepVerifier.create(priceService.getPriceById(priceId))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(actualPriceRepository, times(1)).findById(priceId);
    }

    @Test
    void getAllPrices_ShouldReturnPrices() {
        ActualPrice price = new ActualPrice();
        price.setId(1L);
        price.setProductId(1L);
        price.setPrice(45000);
        Pageable pageable = PageRequest.of(0, 10);

        when(actualPriceRepository.findAll()).thenReturn(Flux.just(price));
        when(actualPriceRepository.count()).thenReturn(Mono.just(1L));

        StepVerifier.create(priceService.getAllPrices(pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1, result.getTotalElements());
                    assertEquals(45000, result.getContent().get(0).getPrice());
                })
                .verifyComplete();

        verify(actualPriceRepository, times(1)).findAll();
        verify(actualPriceRepository, times(1)).count();
    }

    @Test
    void getAllPrices_WithPagination_ShouldReturnPaginatedPrices() {
        ActualPrice price1 = new ActualPrice();
        price1.setId(1L);
        price1.setProductId(1L);
        price1.setPrice(45000);

        ActualPrice price2 = new ActualPrice();
        price2.setId(2L);
        price2.setProductId(2L);
        price2.setPrice(50000);

        ActualPrice price3 = new ActualPrice();
        price3.setId(3L);
        price3.setProductId(3L);
        price3.setPrice(55000);

        Pageable pageable = PageRequest.of(1, 1); // Вторая страница, размер 1

        when(actualPriceRepository.findAll()).thenReturn(Flux.just(price1, price2, price3));
        when(actualPriceRepository.count()).thenReturn(Mono.just(3L));

        StepVerifier.create(priceService.getAllPrices(pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(3, result.getTotalElements());
                    assertEquals(1, result.getContent().size());
                    assertEquals(50000, result.getContent().get(0).getPrice());
                })
                .verifyComplete();
    }

    @Test
    void getAllPrices_WhenNoPrices_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(actualPriceRepository.findAll()).thenReturn(Flux.empty());
        when(actualPriceRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(priceService.getAllPrices(pageable))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(0, result.getTotalElements());
                    assertTrue(result.getContent().isEmpty());
                })
                .verifyComplete();
    }
}

