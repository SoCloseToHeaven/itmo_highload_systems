package ru.ifmo.highload.api;

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
}
