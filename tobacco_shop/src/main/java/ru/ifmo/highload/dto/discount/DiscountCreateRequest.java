package ru.ifmo.highload.dto.discount;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiscountCreateRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Actual price ID is required")
    private Long actualPriceId;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
}
