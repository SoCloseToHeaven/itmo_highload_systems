package ru.ifmo.highload.order.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Auth via X-User-Id and X-User-Roles headers (set by Gateway). Forwards them to Feign via ExchangeHolder.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XUserIdWebFilter implements WebFilter {

    public static final String HEADER_X_USER_ID = "X-User-Id";
    public static final String HEADER_X_USER_ROLES = "X-User-Roles";
    public static final String ATTR_USER_ID = "gateway.userId";
    public static final String ATTR_USER_ROLES = "gateway.userRoles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userIdHeader = exchange.getRequest().getHeaders().getFirst(HEADER_X_USER_ID);
        String rolesHeader = exchange.getRequest().getHeaders().getFirst(HEADER_X_USER_ROLES);

        if (!StringUtils.hasText(userIdHeader)) {
            return chain.filter(exchange);
        }

        try {
            Long userId = Long.parseLong(userIdHeader.trim());
            List<String> roles = parseRolesList(rolesHeader);
            exchange.getAttributes().put(ATTR_USER_ID, userId);
            exchange.getAttributes().put(ATTR_USER_ROLES, rolesHeader != null ? rolesHeader : "");

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());
            CurrentUser currentUser = new CurrentUser(userId, null, roles);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(currentUser, null, authorities);

            ExchangeHolder.set(exchange);
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                    .doFinally(s -> ExchangeHolder.clear());
        } catch (NumberFormatException e) {
            return chain.filter(exchange);
        }
    }

    private static List<String> parseRolesList(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return Collections.emptyList();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
