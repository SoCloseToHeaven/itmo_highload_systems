package ru.ifmo.highload.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pricing-service", path = "/api/price")
public interface PriceServiceClient {

    @GetMapping("/product/{productId}/current")
    @CircuitBreaker(name = "pricingService", fallbackMethod = "getCurrentPriceForProductFallback")
    Integer getCurrentPriceForProduct(@PathVariable Long productId);

    default Integer getCurrentPriceForProductFallback(Long productId, Exception e) {
        throw new RuntimeException("Price service unavailable: " + e.getMessage());
    }
}

