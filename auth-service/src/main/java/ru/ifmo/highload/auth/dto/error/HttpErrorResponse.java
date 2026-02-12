package ru.ifmo.highload.auth.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Ответ с информацией об ошибке")
public class HttpErrorResponse {

    @Schema(description = "HTTP статус", example = "400")
    private int status;

    @Schema(description = "URL", example = "/api/auth/error")
    private String path;

    @Schema(description = "Описание ошибки", example = "Error")
    private String error;

    @Schema(description = "Время ошибки")
    private ZonedDateTime timestamp;
}
