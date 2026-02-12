package ru.ifmo.highload.auth.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.highload.auth.impl.user.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil unit tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-min-32-characters-long");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    }

    @Test
    @DisplayName("generateToken produces valid token")
    void generateToken_producesValidToken() {
        String token = jwtUtil.generateToken(1L, "user1", List.of(Role.USER));

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("getUsernameFromToken extracts subject")
    void getUsernameFromToken() {
        String token = jwtUtil.generateToken(10L, "supervisor", List.of(Role.SUPERVISOR));

        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("supervisor");
    }

    @Test
    @DisplayName("getUserIdFromToken extracts userId claim")
    void getUserIdFromToken() {
        String token = jwtUtil.generateToken(42L, "user", List.of(Role.USER));

        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("getRolesFromToken extracts roles")
    void getRolesFromToken() {
        String token = jwtUtil.generateToken(1L, "logistician", List.of(Role.LOGISTICIAN));

        List<String> roles = jwtUtil.getRolesFromToken(token);
        assertThat(roles).containsExactly("LOGISTICIAN");
    }

    @Test
    @DisplayName("getRolesFromToken returns multiple roles")
    void getRolesFromToken_multiple() {
        String token = jwtUtil.generateToken(1L, "admin",
                List.of(Role.SUPERVISOR, Role.USER));

        List<String> roles = jwtUtil.getRolesFromToken(token);
        assertThat(roles).containsExactlyInAnyOrder("SUPERVISOR", "USER");
    }

    @Test
    @DisplayName("parseToken returns claims with subject and userId")
    void parseToken() {
        String token = jwtUtil.generateToken(5L, "cashier", List.of(Role.CASHIER));
        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("cashier");
        assertThat(claims.get("userId", Long.class)).isEqualTo(5L);
    }

    @Test
    @DisplayName("validateToken returns false for invalid token")
    void validateToken_invalid() {
        assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for empty string")
    void validateToken_empty() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for tampered token")
    void validateToken_tampered() {
        String validToken = jwtUtil.generateToken(1L, "u", List.of(Role.USER));
        String tampered = validToken.substring(0, validToken.length() - 2) + "xx";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("works with short secret (padding branch)")
    void shortSecret_padding() {
        JwtUtil shortSecretUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortSecretUtil, "secret", "short");
        ReflectionTestUtils.setField(shortSecretUtil, "expirationMs", 3600000L);

        String token = shortSecretUtil.generateToken(1L, "u", List.of(Role.USER));
        assertThat(shortSecretUtil.validateToken(token)).isTrue();
        assertThat(shortSecretUtil.getUsernameFromToken(token)).isEqualTo("u");
    }
}
