package ru.ifmo.highload.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

import java.util.List;

public interface DiscountService {
    DiscountResponse createDiscount(DiscountCreateRequest request);

    DiscountResponse updateDiscount(Long discountId, DiscountUpdateRequest request);

    void deleteDiscount(Long discountId);

    List<DiscountResponse> getActiveDiscounts();

    Page<DiscountResponse> getAllDiscounts(Pageable pageable);
}
