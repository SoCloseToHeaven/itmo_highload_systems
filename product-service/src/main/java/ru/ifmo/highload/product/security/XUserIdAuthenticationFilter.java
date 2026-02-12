package ru.ifmo.highload.product.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Auth via X-User-Id and X-User-Roles headers (set by Gateway after JWT validation).
 */
@Component
public class XUserIdAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_X_USER_ID = "X-User-Id";
    public static final String HEADER_X_USER_ROLES = "X-User-Roles";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader(HEADER_X_USER_ID);
            String rolesHeader = request.getHeader(HEADER_X_USER_ROLES);

            if (StringUtils.hasText(userIdHeader)) {
                Long userId = Long.parseLong(userIdHeader.trim());
                List<SimpleGrantedAuthority> authorities = parseRoles(rolesHeader);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (NumberFormatException ignored) {
        }

        filterChain.doFilter(request, response);
    }

    private static List<SimpleGrantedAuthority> parseRoles(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return Collections.emptyList();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }
}
