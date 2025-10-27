package ru.ifmo.highload.api;

import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

import java.util.List;

public interface DiscountService {
    DiscountResponse createDiscount(DiscountCreateRequest request);
    DiscountResponse updateDiscount(Long discountId, DiscountUpdateRequest request);
    void deleteDiscount(Long discountId);
    List<DiscountResponse> getActiveDiscounts();
}
