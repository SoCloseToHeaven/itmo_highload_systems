package ru.ifmo.highload.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

public interface PriceService {
    PriceResponse createPrice(PriceCreateRequest request);

    PriceResponse updatePrice(Long priceId, PriceUpdateRequest request);

    PriceResponse updatePriceByProductId(Long productId, PriceUpdateRequest request);

    void deletePrice(Long priceId);

    void deletePriceByProductId(Long productId);

    Integer getCurrentPriceForProduct(Long productId);

    PriceResponse getPriceById(Long priceId);

    Page<PriceResponse> getAllPrices(Pageable pageable);
}
