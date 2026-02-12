package ru.ifmo.highload.price.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.price.api.PriceService;
import ru.ifmo.highload.price.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.price.dto.actual_price.PriceResponse;
import ru.ifmo.highload.price.dto.actual_price.PriceUpdateRequest;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController implements PriceApi {

    private final PriceService priceService;

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public Mono<ResponseEntity<PriceResponse>> createPrice(PriceCreateRequest request) {
        return priceService.createPrice(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public Mono<ResponseEntity<PriceResponse>> updatePrice(Long priceId, PriceUpdateRequest request) {
        return priceService.updatePrice(priceId, request)
                .map(ResponseEntity::ok);
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public Mono<ResponseEntity<Void>> deletePrice(Long priceId) {
        return priceService.deletePrice(priceId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public Mono<ResponseEntity<PriceResponse>> updatePriceByProduct(Long productId, PriceUpdateRequest request) {
        return priceService.updatePriceByProductId(productId, request)
                .map(ResponseEntity::ok);
    }

    @Override
    @PreAuthorize("hasAnyRole('LOGISTICIAN','SUPERVISOR')")
    public Mono<ResponseEntity<Void>> deletePriceByProduct(Long productId) {
        return priceService.deletePriceByProductId(productId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<Integer>> getCurrentPriceForProduct(Long productId) {
        return priceService.getCurrentPriceForProduct(productId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Page<PriceResponse>>> getAllPrices(Pageable pageable) {
        return priceService.getAllPrices(pageable)
                .map(ResponseEntity::ok);
    }
}

