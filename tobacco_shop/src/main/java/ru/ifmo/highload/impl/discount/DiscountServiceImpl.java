package ru.ifmo.highload.impl.discount;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.api.DiscountService;
import ru.ifmo.highload.api.PriceService;
import ru.ifmo.highload.api.ProductService;
import ru.ifmo.highload.dto.discount.DiscountCreateRequest;
import ru.ifmo.highload.dto.discount.DiscountResponse;
import ru.ifmo.highload.dto.discount.DiscountUpdateRequest;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductService productService;
    private final PriceService priceService;

    @Override
    @Transactional
    public DiscountResponse createDiscount(DiscountCreateRequest request) {
        try {
            productService.getProductById(request.getProductId());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найден продукт с id: " + request.getProductId());
        }

        try {
            priceService.getPriceById(request.getActualPriceId());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найдена цена с id: " + request.getActualPriceId());
        }

        try {
            priceService.getCurrentPriceForProduct(request.getProductId());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найдена цена для продукта с id: " + request.getProductId());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Дата конца должна быть позже даты начала");
        }

        Discount discount = new Discount();
        discount.setProductId(request.getProductId());
        discount.setActualPriceId(request.getActualPriceId());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());

        Discount saved = discountRepository.save(discount);
        return toDiscountResponse(saved);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long discountId, DiscountUpdateRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найдена скидка с id: " + discountId));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Дата конца должна быть позже даты начала");
        }

        try {
            priceService.getPriceById(request.getActualPriceId());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Не найдена цена с id: " + request.getActualPriceId());
        }

        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setActualPriceId(request.getActualPriceId());

        Discount updated = discountRepository.save(discount);
        return toDiscountResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long discountId) {
        if (!discountRepository.existsById(discountId)) {
            throw new ResourceNotFoundException("Не найдена скидка с id: " + discountId);
        }
        discountRepository.deleteById(discountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getActiveDiscounts() {
        ZonedDateTime now = ZonedDateTime.now();
        return discountRepository.findByStartDateBeforeAndEndDateAfter(now, now)
                .stream()
                .map(this::toDiscountResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DiscountResponse> getAllDiscounts(Pageable pageable) {
        return discountRepository.findAll(pageable).map(this::toDiscountResponse);
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
