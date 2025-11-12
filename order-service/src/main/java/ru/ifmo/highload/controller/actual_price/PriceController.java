package ru.ifmo.highload.controller.actual_price;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController implements PriceApi {

    private final PriceService priceService;

    @Override
    public ResponseEntity<PriceResponse> createPrice(PriceCreateRequest request) {
        return ResponseEntity.ok(priceService.createPrice(request));
    }

    @Override
    public ResponseEntity<PriceResponse> updatePrice(Long priceId, PriceUpdateRequest request) {
        return ResponseEntity.ok(priceService.updatePrice(priceId, request));
    }

    @Override
    public ResponseEntity<Void> deletePrice(Long priceId) {
        priceService.deletePrice(priceId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PriceResponse> updatePriceByProduct(Long productId, PriceUpdateRequest request) {
        return ResponseEntity.ok(priceService.updatePriceByProductId(productId, request));
    }

    @Override
    public ResponseEntity<Void> deletePriceByProduct(Long productId) {
        priceService.deletePriceByProductId(productId);
        return ResponseEntity.ok().build();
    }
}
