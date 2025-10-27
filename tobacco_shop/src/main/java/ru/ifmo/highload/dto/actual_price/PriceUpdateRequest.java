package ru.ifmo.highload.dto.actual_price;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PriceUpdateRequest {
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Integer price;
}
