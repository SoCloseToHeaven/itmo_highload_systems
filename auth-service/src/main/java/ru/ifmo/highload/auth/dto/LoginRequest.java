package ru.ifmo.highload.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на вход")
public class LoginRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    @Schema(description = "Имя пользователя", required = true)
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль", required = true)
    private String password;
}
