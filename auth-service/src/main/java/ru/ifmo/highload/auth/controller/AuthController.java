package ru.ifmo.highload.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.highload.auth.dto.LoginRequest;
import ru.ifmo.highload.auth.dto.LoginResponse;
import ru.ifmo.highload.auth.dto.UserCreateRequest;
import ru.ifmo.highload.auth.dto.UserResponse;
import ru.ifmo.highload.auth.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Авторизация и управление пользователями")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход", description = "Возвращает JWT токен для заголовка Authorization: Bearer <token>")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/users")
    @Operation(summary = "Создать пользователя", description = "Только супервайзер может создавать пользователей")
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users")
    @Operation(summary = "Список пользователей", description = "Только супервайзер")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь", description = "Данные текущего пользователя (пароль не отдаётся)")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(authService.getCurrentUser(currentUser.getUsername()));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Пользователь по ID", description = "Только супервайзер")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }
}
