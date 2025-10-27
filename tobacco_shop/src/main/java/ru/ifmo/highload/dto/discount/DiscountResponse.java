package ru.ifmo.highload.dto.discount;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiscountResponse {
    private Long id;
    private Long productId;
    private Long actualPriceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
