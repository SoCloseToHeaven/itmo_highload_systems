package ru.ifmo.highload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.api.DiscountService;
import ru.ifmo.highload.controller.discount.DiscountApi;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

@RestController
@RequestMapping("/api/discount")
@RequiredArgsConstructor
public class DiscountController implements DiscountApi {

    private final DiscountService discountService;

    @Override
    public Mono<ResponseEntity<DiscountResponse>> createDiscount(DiscountCreateRequest request) {
        return discountService.createDiscount(request)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<DiscountResponse>> updateDiscount(Long discountId, DiscountUpdateRequest request) {
        return discountService.updateDiscount(discountId, request)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDiscount(Long discountId) {
        return discountService.deleteDiscount(discountId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<DiscountResponse>>> getActiveDiscounts() {
        return Mono.just(ResponseEntity.ok(discountService.getActiveDiscounts()));
    }
}
