package ru.ifmo.highload.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.ifmo.highload.auth.impl.user.UserRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Auth via X-User-Id header (set by Gateway). Loads user from DB and sets SecurityContext for role checks.
 */
@Component
@RequiredArgsConstructor
public class XUserIdAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_X_USER_ID = "X-User-Id";
    public static final String HEADER_X_USER_ROLES = "X-User-Roles";

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader(HEADER_X_USER_ID);
            if (StringUtils.hasText(userIdHeader)) {
                Long userId = Long.parseLong(userIdHeader.trim());
                userRepository.findById(userId).ifPresent(user -> {
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    UserDetails userDetails = User.withUsername(user.getUsername())
                            .password("")
                            .authorities(authorities)
                            .build();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        } catch (NumberFormatException ignored) {
        }

        filterChain.doFilter(request, response);
    }
}
