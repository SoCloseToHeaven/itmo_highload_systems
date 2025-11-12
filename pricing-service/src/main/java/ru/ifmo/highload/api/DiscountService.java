package ru.ifmo.highload.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

public interface DiscountService {
    Mono<DiscountResponse> createDiscount(DiscountCreateRequest request);

    Mono<DiscountResponse> updateDiscount(Long discountId, DiscountUpdateRequest request);

    Mono<Void> deleteDiscount(Long discountId);

    Flux<DiscountResponse> getActiveDiscounts();
}
