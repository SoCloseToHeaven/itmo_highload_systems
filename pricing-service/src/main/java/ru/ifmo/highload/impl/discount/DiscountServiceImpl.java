package ru.ifmo.highload.impl.discount;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.api.DiscountService;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.client.ProductServiceClient;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductServiceClient productServiceClient;
    private final PriceService priceService;

    @Override
    public Mono<DiscountResponse> createDiscount(DiscountCreateRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return Mono.error(new RuntimeException("End date must be after start date"));
        }

        return Mono.fromCallable(() -> productServiceClient.getProductById(request.getProductId()))
                .flatMap(product -> priceService.getCurrentPriceForProduct(request.getProductId())
                        .flatMap(price -> {
                            Discount discount = Discount.builder()
                                    .productId(request.getProductId())
                                    .actualPriceId(request.getActualPriceId())
                                    .startDate(request.getStartDate())
                                    .endDate(request.getEndDate())
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            return discountRepository.save(discount)
                                    .map(this::toDiscountResponse);
                        }))
                .onErrorMap(e -> new RuntimeException("Product or price not found", e));
    }

    @Override
    public Mono<DiscountResponse> updateDiscount(Long discountId, DiscountUpdateRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return Mono.error(new RuntimeException("End date must be after start date"));
        }

        return discountRepository.findById(discountId)
                .switchIfEmpty(Mono.error(new RuntimeException("Discount not found with id: " + discountId)))
                .flatMap(discount -> {
                    discount.setStartDate(request.getStartDate());
                    discount.setEndDate(request.getEndDate());
                    discount.setActualPriceId(request.getActualPriceId());
                    discount.setUpdatedAt(LocalDateTime.now());
                    return discountRepository.save(discount)
                            .map(this::toDiscountResponse);
                });
    }

    @Override
    public Mono<Void> deleteDiscount(Long discountId) {
        return discountRepository.existsById(discountId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Discount not found with id: " + discountId));
                    }
                    return discountRepository.deleteById(discountId);
                });
    }

    @Override
    public Flux<DiscountResponse> getActiveDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findByStartDateBeforeAndEndDateAfter(now, now)
                .map(this::toDiscountResponse);
    }

    private DiscountResponse toDiscountResponse(Discount discount) {
        DiscountResponse response = new DiscountResponse();
        response.setId(discount.getId());
        response.setProductId(discount.getProductId());
        response.setActualPriceId(discount.getActualPriceId());
        response.setStartDate(discount.getStartDate());
        response.setEndDate(discount.getEndDate());
        return response;
    }
}
