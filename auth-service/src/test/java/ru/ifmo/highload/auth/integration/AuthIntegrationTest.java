package ru.ifmo.highload.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.ifmo.highload.auth.config.TestcontainersConfiguration;
import ru.ifmo.highload.auth.dto.LoginRequest;
import ru.ifmo.highload.auth.dto.UserCreateRequest;
import ru.ifmo.highload.auth.impl.user.Role;
import ru.ifmo.highload.auth.impl.user.User;
import ru.ifmo.highload.auth.impl.user.UserRepository;
import ru.ifmo.highload.auth.security.XUserIdAuthenticationFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthIntegrationTest extends TestcontainersConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SUPERVISOR_PASSWORD = "password";
    private Long supervisorId;

    @BeforeEach
    void ensureSupervisorWithKnownPassword() {
        userRepository.findByUsername("supervisor").ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode(SUPERVISOR_PASSWORD));
                    userRepository.save(user);
                },
                () -> {
                    User u = new User();
                    u.setUsername("supervisor");
                    u.setPasswordHash(passwordEncoder.encode(SUPERVISOR_PASSWORD));
                    u.setRole(Role.SUPERVISOR);
                    userRepository.save(u);
                }
        );
        supervisorId = userRepository.findByUsername("supervisor").orElseThrow().getId();
    }

    @Test
    void login_WithSupervisor_ShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("supervisor");
        request.setPassword(SUPERVISOR_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    void login_WithWrongPassword_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("supervisor");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithUnknownUser_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_AsSupervisor_ShouldReturn201() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("pass1234");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/auth/users")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_WithoutAuth_ShouldReturn403() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("pass1234");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMe_WithValidToken_ShouldReturnUserWithoutPassword() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("supervisor"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getMe_WithoutToken_ShouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllUsers_AsSupervisor_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/auth/users")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("supervisor"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void createUser_DuplicateUsername_ShouldReturn400() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("supervisor");
        request.setPassword("other");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/auth/users")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_AsSupervisor_ShouldReturnUser() throws Exception {
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setUsername("userforid");
        createRequest.setPassword("pass1234");
        createRequest.setRole(Role.CASHIER);

        String createResponse = mockMvc.perform(post("/api/auth/users")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/auth/users/" + userId)
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("userforid"))
                .andExpect(jsonPath("$.role").value("CASHIER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/auth/users/99999")
                        .header(XUserIdAuthenticationFilter.HEADER_X_USER_ID, String.valueOf(supervisorId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/auth/users/1"))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String username, String pwd) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(pwd);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
