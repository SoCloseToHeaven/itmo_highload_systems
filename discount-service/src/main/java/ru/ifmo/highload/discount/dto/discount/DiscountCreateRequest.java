package ru.ifmo.highload.discount.dto.discount;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Запрос на создание скидки")
public class DiscountCreateRequest {

    @NotNull(message = "ID товара обязателен")
    @Schema(description = "ID товара", example = "1", required = true)
    private Long productId;

    @NotNull(message = "ID актуальной цены обязателен")
    @Schema(description = "ID актуальной цены", example = "1", required = true)
    private Long actualPriceId;

    @NotNull(message = "Дата начала обязательна")
    @Schema(description = "Дата начала действия скидки", example = "2025-09-28T03:35:07.528+03:00", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startDate;

    @NotNull(message = "Дата окончания обязательна")
    @Future(message = "Дата окончания должна быть в будущем")
    @Schema(description = "Дата окончания действия скидки", example = "2025-12-28T03:35:07.528+03:00", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime endDate;
}

