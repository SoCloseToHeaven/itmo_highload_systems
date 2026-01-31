package ru.ifmo.highload.discount.dto.discount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.ZonedDateTime;

@Data
@Schema(description = "Ответ с информацией о скидке")
public class DiscountResponse {

    @Schema(description = "ID скидки", example = "1")
    private Long id;

    @Schema(description = "ID товара", example = "1")
    private Long productId;

    @Schema(description = "ID актуальной цены", example = "1")
    private Long actualPriceId;

    @Schema(description = "Дата начала действия скидки", example = "2025-09-28T03:35:07.528+03:00")
    private ZonedDateTime startDate;

    @Schema(description = "Дата окончания действия скидки", example = "2025-12-28T03:35:07.528+03:00")
    private ZonedDateTime endDate;
}

