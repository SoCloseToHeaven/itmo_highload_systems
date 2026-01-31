package ru.ifmo.highload.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ifmo.highload.order.security.JwtContextHolder;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            String token = JwtContextHolder.getToken();
            if (token != null && !token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
