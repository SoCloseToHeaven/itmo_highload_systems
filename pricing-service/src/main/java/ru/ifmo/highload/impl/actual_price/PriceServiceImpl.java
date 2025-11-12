package ru.ifmo.highload.impl.actual_price;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.client.ProductServiceClient;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final ActualPriceRepository actualPriceRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public Mono<PriceResponse> createPrice(PriceCreateRequest request) {
        return Mono.fromCallable(() -> productServiceClient.getProductById(request.getProductId()))
                .flatMap(product -> actualPriceRepository.existsByProductId(request.getProductId())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new RuntimeException("Price already exists for this product"));
                            }
                            ActualPrice price = ActualPrice.builder()
                                    .productId(request.getProductId())
                                    .price(request.getPrice())
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            return actualPriceRepository.save(price)
                                    .map(this::toPriceResponse);
                        }))
                .onErrorMap(e -> new RuntimeException("Product not found with id: " + request.getProductId(), e));
    }

    @Override
    public Mono<PriceResponse> updatePrice(Long priceId, PriceUpdateRequest request) {
        return actualPriceRepository.findById(priceId)
                .switchIfEmpty(Mono.error(new RuntimeException("Price not found with id: " + priceId)))
                .flatMap(price -> {
                    price.setPrice(request.getPrice());
                    price.setUpdatedAt(LocalDateTime.now());
                    return actualPriceRepository.save(price)
                            .map(this::toPriceResponse);
                });
    }

    @Override
    public Mono<PriceResponse> updatePriceByProductId(Long productId, PriceUpdateRequest request) {
        return actualPriceRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Price not found for product with id: " + productId)))
                .flatMap(price -> {
                    price.setPrice(request.getPrice());
                    price.setUpdatedAt(LocalDateTime.now());
                    return actualPriceRepository.save(price)
                            .map(this::toPriceResponse);
                });
    }

    @Override
    public Mono<Void> deletePrice(Long priceId) {
        return actualPriceRepository.existsById(priceId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Price not found with id: " + priceId));
                    }
                    return actualPriceRepository.deleteById(priceId);
                });
    }

    @Override
    public Mono<Void> deletePriceByProductId(Long productId) {
        return actualPriceRepository.deleteByProductId(productId);
    }

    @Override
    public Mono<Integer> getCurrentPriceForProduct(Long productId) {
        return actualPriceRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Price not found for product with id: " + productId)))
                .map(ActualPrice::getPrice);
    }

    private PriceResponse toPriceResponse(ActualPrice price) {
        PriceResponse response = new PriceResponse();
        response.setId(price.getId());
        response.setProductId(price.getProductId());
        response.setPrice(price.getPrice());
        return response;
    }
}
