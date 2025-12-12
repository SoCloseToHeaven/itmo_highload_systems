package ru.ifmo.highload.impl.actual_price;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.actual_price.PriceCreateRequest;
import ru.ifmo.highload.dto.actual_price.PriceResponse;
import ru.ifmo.highload.dto.actual_price.PriceUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final ActualPriceRepository actualPriceRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public PriceResponse createPrice(PriceCreateRequest request) {
        try {
            productService.getProductById(request.getProductId());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найден продукт с id: " + request.getProductId());
        }

        if (actualPriceRepository.existsByProductId(request.getProductId())) {
            throw new BadRequestException("Для данного продукта цена уже существует");
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
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена цена с id: " + priceId));

        price.setPrice(request.getPrice());

        ActualPrice updated = actualPriceRepository.save(price);
        return toPriceResponse(updated);
    }

    @Override
    @Transactional
    public PriceResponse updatePriceByProductId(Long productId, PriceUpdateRequest request) {
        ActualPrice price = actualPriceRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена цена для продукта с id: " + productId));

        price.setPrice(request.getPrice());

        ActualPrice updated = actualPriceRepository.save(price);
        return toPriceResponse(updated);
    }

    @Override
    @Transactional
    public void deletePrice(Long priceId) {
        if (!actualPriceRepository.existsById(priceId)) {
            throw new ResourceNotFoundException("Не найдена цена с id: " + priceId);
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
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена цена для продукта с id: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public PriceResponse getPriceById(Long id) {
        ActualPrice price = actualPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена цена с id: " + id));
        return toPriceResponse(price);
    }

    private PriceResponse toPriceResponse(ActualPrice price) {
        PriceResponse response = new PriceResponse();
        response.setId(price.getId());
        response.setProductId(price.getProductId());
        response.setPrice(price.getPrice());
        return response;
    }
}
