package ru.ifmo.highload.dto.discount;

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

    @Schema(description = "Дата начала действия скидки", example = "2024-01-01T00:00:00")
    private ZonedDateTime startDate;

    @Schema(description = "Дата окончания действия скидки", example = "2024-12-31T23:59:59")
    private ZonedDateTime endDate;
}
