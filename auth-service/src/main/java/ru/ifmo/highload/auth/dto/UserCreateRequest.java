package ru.ifmo.highload.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.ifmo.highload.auth.impl.user.Role;

@Data
@Schema(description = "Запрос на создание пользователя (только для супервайзера)")
public class UserCreateRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 1, max = 255)
    @Schema(description = "Имя пользователя", required = true)
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 4, message = "Пароль должен быть не менее 4 символов")
    @Schema(description = "Пароль", required = true)
    private String password;

    @NotNull(message = "Роль обязательна")
    @Schema(description = "Роль пользователя", required = true)
    private Role role;
}
