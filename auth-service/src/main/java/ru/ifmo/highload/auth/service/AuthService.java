package ru.ifmo.highload.auth.service;

import ru.ifmo.highload.auth.dto.LoginRequest;
import ru.ifmo.highload.auth.dto.LoginResponse;
import ru.ifmo.highload.auth.dto.UserCreateRequest;
import ru.ifmo.highload.auth.dto.UserResponse;

import java.util.List;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getCurrentUser(String username);

    UserResponse getUserById(Long id);
}
