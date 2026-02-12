package ru.ifmo.highload.auth.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl unit tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";
    private static final String HASH = "$2a$10$hash";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername(USERNAME);
        user.setPasswordHash(HASH);
        user.setRole(Role.USER);
    }

    @Test
    @DisplayName("login returns token when credentials are valid")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
        when(jwtUtil.generateToken(1L, USERNAME, Collections.singletonList(Role.USER)))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches(PASSWORD, HASH);
        verify(jwtUtil).generateToken(1L, USERNAME, Collections.singletonList(Role.USER));
    }

    @Test
    @DisplayName("login throws when user not found")
    void login_userNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword(PASSWORD);

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Неверное имя пользователя или пароль");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any(), anyString(), any());
    }

    @Test
    @DisplayName("login throws when password is wrong")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword("wrong");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Неверное имя пользователя или пароль");
        verify(jwtUtil, never()).generateToken(any(), anyString(), any());
    }

    @Test
    @DisplayName("createUser saves user and returns response without password")
    void createUser_success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("pass");
        request.setRole(Role.CASHIER);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPasswordHash("encoded");
        savedUser.setRole(Role.CASHIER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.createUser(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo(Role.CASHIER);
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser throws when username already exists")
    void createUser_duplicateUsername() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setRole(Role.USER);

        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.createUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Пользователь с таким именем уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getAllUsers returns list of user responses")
    void getAllUsers_success() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("second");
        user2.setRole(Role.LOGISTICIAN);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserResponse> result = authService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getUsername()).isEqualTo(USERNAME);
        assertThat(result.get(0).getRole()).isEqualTo(Role.USER);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getUsername()).isEqualTo("second");
        assertThat(result.get(1).getRole()).isEqualTo(Role.LOGISTICIAN);
    }

    @Test
    @DisplayName("getAllUsers returns empty list when no users")
    void getAllUsers_empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = authService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCurrentUser returns user by username")
    void getCurrentUser_success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser(USERNAME);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo(USERNAME);
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("getCurrentUser throws when user not found")
    void getCurrentUser_notFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("getUserById returns user")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = authService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("getUserById throws when not found")
    void getUserById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 999");
    }
}
