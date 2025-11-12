package ru.ifmo.highload.impl.actual_price;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final ActualPriceRepository actualPriceRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public PriceResponse createPrice(PriceCreateRequest request) {
        // Используем ProductService для проверки существования продукта
        try {
            productService.getProductById(request.getProductId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Product not found with id: " + request.getProductId());
        }

        if (actualPriceRepository.existsByProductId(request.getProductId())) {
            throw new RuntimeException("Price already exists for this product");
        }

        ActualPrice price = new ActualPrice();
        price.setProductId(request.getProductId());
        price.setPrice(request.getPrice());

        ActualPrice saved = actualPriceRepository.save(price);
        return toPriceResponse(saved);
    }

    @Override
    @Transactional
    public PriceResponse updatePrice(Long priceId, PriceUpdateRequest request) {
        ActualPrice price = actualPriceRepository.findById(priceId)
                .orElseThrow(() -> new RuntimeException("Price not found with id: " + priceId));

        price.setPrice(request.getPrice());

        ActualPrice updated = actualPriceRepository.save(price);
        return toPriceResponse(updated);
    }

    @Override
    @Transactional
    public PriceResponse updatePriceByProductId(Long productId, PriceUpdateRequest request) {
        ActualPrice price = actualPriceRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Price not found for product with id: " + productId));

        price.setPrice(request.getPrice());

        ActualPrice updated = actualPriceRepository.save(price);
        return toPriceResponse(updated);
    }

    @Override
    @Transactional
    public void deletePrice(Long priceId) {
        if (!actualPriceRepository.existsById(priceId)) {
            throw new RuntimeException("Price not found with id: " + priceId);
        }
        actualPriceRepository.deleteById(priceId);
    }

    @Override
    @Transactional
    public void deletePriceByProductId(Long productId) {
        actualPriceRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentPriceForProduct(Long productId) {
        return actualPriceRepository.findByProductId(productId)
                .map(ActualPrice::getPrice)
                .orElseThrow(() -> new RuntimeException("Price not found for product with id: " + productId));
    }

    private PriceResponse toPriceResponse(ActualPrice price) {
        PriceResponse response = new PriceResponse();
        response.setId(price.getId());
        response.setProductId(price.getProductId());
        response.setPrice(price.getPrice());
        return response;
    }
}
