package ru.ifmo.highload.price.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.price.dto.error.HttpErrorResponse;

import java.time.ZonedDateTime;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, e) -> writeJsonError(exchange, "Not authenticated", HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((exchange, denied) -> writeJsonError(exchange, "Access denied", HttpStatus.FORBIDDEN)))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.GET, "/api/price/product/*/current").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/price").permitAll()
                        .pathMatchers("/api/price/**").authenticated()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/swagger-ui/**").permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    private Mono<Void> writeJsonError(org.springframework.web.server.ServerWebExchange exchange, String error, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        HttpErrorResponse body = new HttpErrorResponse();
        body.setPath(exchange.getRequest().getPath().value());
        body.setError(error);
        body.setStatus(status.value());
        body.setTimestamp(ZonedDateTime.now());
        try {
            DataBuffer buf = exchange.getResponse().bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
            return exchange.getResponse().writeWith(Mono.just(buf));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
