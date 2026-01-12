package ru.ifmo.highload.discount.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.discount.dto.external.price.PriceResponse;

@Slf4j
@Component
public class PriceServiceClientFallback implements PriceServiceClient {

    @Override
    public PriceResponse getPriceById(Long id) {
        log.error("PriceServiceClient fallback called for priceId: {}", id);
        throw new RuntimeException("Price service unavailable");
    }
}

