package ru.ifmo.highload.price.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.price.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.price.dto.actual_price.PriceResponse;
import ru.ifmo.highload.price.dto.actual_price.PriceUpdateRequest;

public interface PriceService {
    Mono<PriceResponse> createPrice(PriceCreateRequest request);

    Mono<PriceResponse> updatePrice(Long priceId, PriceUpdateRequest request);

    Mono<PriceResponse> updatePriceByProductId(Long productId, PriceUpdateRequest request);

    Mono<Void> deletePrice(Long priceId);

    Mono<Void> deletePriceByProductId(Long productId);

    Mono<Integer> getCurrentPriceForProduct(Long productId);

    Mono<PriceResponse> getPriceById(Long priceId);

    Mono<Page<PriceResponse>> getAllPrices(Pageable pageable);
}

