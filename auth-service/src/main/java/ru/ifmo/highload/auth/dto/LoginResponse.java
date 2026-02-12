package ru.ifmo.highload.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с JWT токеном")
public class LoginResponse {

    @Schema(description = "JWT токен для заголовка Authorization: Bearer <token>")
    private String token;

    @Schema(description = "Роль пользователя")
    private String role;
}
