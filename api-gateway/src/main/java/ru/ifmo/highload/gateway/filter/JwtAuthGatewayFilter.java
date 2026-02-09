package ru.ifmo.highload.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates JWT at Gateway, extracts userId and roles, adds them to X-User-Id and X-User-Roles headers.
 * Public paths skip JWT; protected paths require valid token.
 */
@Component
@Slf4j
public class JwtAuthGatewayFilter implements WebFilter {

    public static final String HEADER_X_USER_ID = "X-User-Id";
    public static final String HEADER_X_USER_ROLES = "X-User-Roles";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";

    @Value("${jwt.secret}")
    private String secret;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] PUBLIC_PATHS = {
            "POST:/api/auth/login",
            "GET:/api/product/**",
            "GET:/api/category/**",
            "GET:/api/price",
            "GET:/api/price/product/*/current",
            "GET:/api/discount/active",
            "GET:/api/discount",
            "GET:/v3/api-docs/**",
            "GET:/swagger-ui/**",
            "GET:/swagger-ui.html",
            "GET:/webjars/**"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "GET";

        boolean publicPath = false;
        for (String pattern : PUBLIC_PATHS) {
            String[] parts = pattern.split(":", 2);
            if (parts.length == 2 && parts[0].equals(method) && pathMatcher.match(parts[1], path)) {
                publicPath = true;
                break;
            }
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            token = authHeader.substring(BEARER_PREFIX.length()).trim();
        }

        if (token == null || token.isEmpty()) {
            if (!publicPath) {
                return unauthorized(exchange.getResponse());
            }
            return chain.filter(exchange);
        }

        Claims claims;
        try {
            claims = parseToken(token);
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            if (!publicPath) {
                return unauthorized(exchange.getResponse());
            }
            return chain.filter(exchange);
        }

        Long userId = claims.get(USER_ID_CLAIM, Long.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        if (userId == null) {
            if (!publicPath) {
                return unauthorized(exchange.getResponse());
            }
            return chain.filter(exchange);
        }

        String rolesHeader = roles != null && !roles.isEmpty()
                ? roles.stream().map(Object::toString).collect(Collectors.joining(","))
                : "";

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER_X_USER_ID, String.valueOf(userId))
                .header(HEADER_X_USER_ROLES, rolesHeader)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        byte[] body = "{\"error\":\"Unauthorized\",\"message\":\"Требуется аутентификация\"}".getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
