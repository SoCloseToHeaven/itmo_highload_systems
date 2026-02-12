package ru.ifmo.highload.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.highload.auth.impl.user.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными пользователя (пароль не отдаётся)")
public class UserResponse {

    @Schema(description = "ID пользователя")
    private Long id;

    @Schema(description = "Имя пользователя")
    private String username;

    @Schema(description = "Роль пользователя")
    private Role role;
}
