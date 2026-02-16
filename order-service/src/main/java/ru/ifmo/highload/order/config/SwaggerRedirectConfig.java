package ru.ifmo.highload.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class SwaggerRedirectConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerRedirect() {
        return route(GET("/swagger-ui.html"),
                req -> ServerResponse.status(HttpStatus.FOUND)
                        .location(req.uri().resolve("/webjars/swagger-ui/index.html"))
                        .build());
    }
}
