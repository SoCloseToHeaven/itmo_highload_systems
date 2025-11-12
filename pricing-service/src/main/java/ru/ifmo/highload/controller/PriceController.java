package ru.ifmo.highload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.controller.actual_price.PriceApi;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController implements PriceApi {

    private final PriceService priceService;

    @Override
    public Mono<ResponseEntity<PriceResponse>> createPrice(PriceCreateRequest request) {
        return priceService.createPrice(request)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PriceResponse>> updatePrice(Long priceId, PriceUpdateRequest request) {
        return priceService.updatePrice(priceId, request)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deletePrice(Long priceId) {
        return priceService.deletePrice(priceId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @Override
    public Mono<ResponseEntity<PriceResponse>> updatePriceByProduct(Long productId, PriceUpdateRequest request) {
        return priceService.updatePriceByProductId(productId, request)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deletePriceByProduct(Long productId) {
        return priceService.deletePriceByProductId(productId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping("/product/{productId}/current")
    public Mono<ResponseEntity<Integer>> getCurrentPriceForProduct(@PathVariable Long productId) {
        return priceService.getCurrentPriceForProduct(productId)
                .map(ResponseEntity::ok);
    }
}
