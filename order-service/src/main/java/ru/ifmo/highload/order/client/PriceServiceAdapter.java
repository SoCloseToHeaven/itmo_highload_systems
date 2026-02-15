package ru.ifmo.highload.order.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ifmo.highload.order.api.PriceDataService;

@Component
@RequiredArgsConstructor
public class PriceServiceAdapter implements PriceDataService {

    private final PriceServiceClient priceServiceClient;

    @Override
    public Integer getCurrentPriceForProduct(Long productId) {
        return priceServiceClient.getCurrentPriceForProduct(productId);
    }
}
