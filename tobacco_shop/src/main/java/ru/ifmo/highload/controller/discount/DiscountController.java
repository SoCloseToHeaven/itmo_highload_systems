package ru.ifmo.highload.controller.discount;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.api.DiscountService;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/discount")
@RequiredArgsConstructor
public class DiscountController implements DiscountApi {

    private final DiscountService discountService;

    @Override
    public ResponseEntity<DiscountResponse> createDiscount(DiscountCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountService.createDiscount(request));
    }

    @Override
    public ResponseEntity<DiscountResponse> updateDiscount(Long discountId, DiscountUpdateRequest request) {
        return ResponseEntity.ok(discountService.updateDiscount(discountId, request));
    }

    @Override
    public ResponseEntity<Void> deleteDiscount(Long discountId) {
        discountService.deleteDiscount(discountId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<DiscountResponse>> getActiveDiscounts() {
        return ResponseEntity.ok(discountService.getActiveDiscounts());
    }
}
