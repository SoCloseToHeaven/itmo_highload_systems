package ru.ifmo.highload.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import ru.ifmo.highload.order.security.ExchangeHolder;
import ru.ifmo.highload.order.security.XUserIdWebFilter;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            ServerWebExchange exchange = ExchangeHolder.get();
            if (exchange != null && exchange.getRequest() != null) {
                String userId = exchange.getRequest().getHeaders().getFirst(XUserIdWebFilter.HEADER_X_USER_ID);
                String roles = exchange.getRequest().getHeaders().getFirst(XUserIdWebFilter.HEADER_X_USER_ROLES);
                if (userId != null && !userId.isEmpty()) {
                    template.header(XUserIdWebFilter.HEADER_X_USER_ID, userId);
                }
                if (roles != null && !roles.isEmpty()) {
                    template.header(XUserIdWebFilter.HEADER_X_USER_ROLES, roles);
                }
            }
        };
    }
}
