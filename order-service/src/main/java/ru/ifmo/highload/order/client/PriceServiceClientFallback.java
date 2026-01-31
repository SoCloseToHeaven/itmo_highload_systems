package ru.ifmo.highload.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.order.dto.external.price.PriceResponse;

@Slf4j
@Component
public class PriceServiceClientFallback implements PriceServiceClient {

    @Override
    public PriceResponse getPriceById(Long id) {
        log.error("PriceServiceClient fallback called for priceId: {}", id);
        throw new RuntimeException("Price service unavailable");
    }

    @Override
    public Integer getCurrentPriceForProduct(Long productId) {
        log.error("PriceServiceClient fallback called for productId: {}", productId);
        throw new RuntimeException("Price service unavailable");
    }
}

