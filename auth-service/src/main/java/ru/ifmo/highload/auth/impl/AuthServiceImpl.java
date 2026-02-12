package ru.ifmo.highload.auth.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.highload.auth.dto.LoginRequest;
import ru.ifmo.highload.auth.dto.LoginResponse;
import ru.ifmo.highload.auth.dto.UserCreateRequest;
import ru.ifmo.highload.auth.dto.UserResponse;
import ru.ifmo.highload.auth.impl.exceptions.BadRequestException;
import ru.ifmo.highload.auth.impl.exceptions.ResourceNotFoundException;
import ru.ifmo.highload.auth.impl.user.Role;
import ru.ifmo.highload.auth.impl.user.User;
import ru.ifmo.highload.auth.impl.user.UserRepository;
import ru.ifmo.highload.auth.security.JwtUtil;
import ru.ifmo.highload.auth.service.AuthService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Неверное имя пользователя или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Неверное имя пользователя или пароль");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
                Collections.singletonList(user.getRole()));

        return new LoginResponse(token, user.getRole().name());
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
