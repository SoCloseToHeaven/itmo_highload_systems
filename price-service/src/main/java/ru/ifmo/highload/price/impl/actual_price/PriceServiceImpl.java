package ru.ifmo.highload.price.impl.actual_price;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.price.api.PriceService;
import ru.ifmo.highload.price.client.ProductServiceClient;
import ru.ifmo.highload.price.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.price.dto.actual_price.PriceResponse;
import ru.ifmo.highload.price.dto.actual_price.PriceUpdateRequest;
import ru.ifmo.highload.price.impl.exceptions.BadRequestException;
import ru.ifmo.highload.price.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final ActualPriceRepository actualPriceRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public Mono<PriceResponse> createPrice(PriceCreateRequest request) {
        return Mono.fromCallable(() -> productServiceClient.getProductById(request.getProductId()))
                .flatMap(product -> actualPriceRepository.countByProductId(request.getProductId())
                        .flatMap(count -> {
                            if (count > 0) {
                                return Mono.error(new BadRequestException("Для данного продукта цена уже существует"));
                            }
                            ActualPrice price = new ActualPrice();
                            price.setProductId(request.getProductId());
                            price.setPrice(request.getPrice());
                            price.setCreatedAt(ZonedDateTime.now());
                            price.setUpdatedAt(ZonedDateTime.now());
                            return actualPriceRepository.save(price);
                        }))
                .onErrorMap(e -> {
                    if (e instanceof BadRequestException || e instanceof ResourceNotFoundException) {
                        return e;
                    }
                    return new ResourceNotFoundException("Не найден продукт с id: " + request.getProductId());
                })
                .map(this::toPriceResponse);
    }

    @Override
    public Mono<PriceResponse> updatePrice(Long priceId, PriceUpdateRequest request) {
        return actualPriceRepository.findById(priceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Не найдена цена с id: " + priceId)))
                .flatMap(price -> {
                    price.setPrice(request.getPrice());
                    price.setUpdatedAt(ZonedDateTime.now());
                    return actualPriceRepository.save(price);
                })
                .map(this::toPriceResponse);
    }

    @Override
    public Mono<PriceResponse> updatePriceByProductId(Long productId, PriceUpdateRequest request) {
        return actualPriceRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Не найдена цена для продукта с id: " + productId)))
                .flatMap(price -> {
                    price.setPrice(request.getPrice());
                    price.setUpdatedAt(ZonedDateTime.now());
                    return actualPriceRepository.save(price);
                })
                .map(this::toPriceResponse);
    }

    @Override
    public Mono<Void> deletePrice(Long priceId) {
        return actualPriceRepository.findById(priceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Не найдена цена с id: " + priceId)))
                .flatMap(price -> actualPriceRepository.deleteById(priceId));
    }

    @Override
    public Mono<Void> deletePriceByProductId(Long productId) {
        return actualPriceRepository.deleteByProductId(productId);
    }

    @Override
    public Mono<Integer> getCurrentPriceForProduct(Long productId) {
        return actualPriceRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Не найдена цена для продукта с id: " + productId)))
                .map(ActualPrice::getPrice);
    }

    @Override
    public Mono<PriceResponse> getPriceById(Long id) {
        return actualPriceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Не найдена цена с id: " + id)))
                .map(this::toPriceResponse);
    }

    @Override
    public Mono<Page<PriceResponse>> getAllPrices(Pageable pageable) {
        Flux<ActualPrice> allPrices = actualPriceRepository.findAll();
        Mono<Long> count = actualPriceRepository.count();
        
        return allPrices
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(this::toPriceResponse)
                .collectList()
                .zipWith(count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    private PriceResponse toPriceResponse(ActualPrice price) {
        PriceResponse response = new PriceResponse();
        response.setId(price.getId());
        response.setProductId(price.getProductId());
        response.setPrice(price.getPrice());
        return response;
    }
}

