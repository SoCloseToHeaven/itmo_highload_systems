package ru.ifmo.highload.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.ifmo.highload.order.dto.external.price.PriceResponse;

@FeignClient(name = "price-service", fallback = PriceServiceClientFallback.class)
public interface PriceServiceClient {

    @GetMapping("/api/price/{id}")
    PriceResponse getPriceById(@PathVariable Long id);

    @GetMapping("/api/price/product/{productId}/current")
    Integer getCurrentPriceForProduct(@PathVariable Long productId);
}

