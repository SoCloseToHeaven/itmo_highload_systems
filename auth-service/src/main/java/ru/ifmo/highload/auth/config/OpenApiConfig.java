package ru.ifmo.highload.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String X_USER_ID = "xUserId";
    private static final String X_USER_ROLES = "xUserRoles";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description("Authentication and user management. 401 = not authenticated; 403 = authenticated but insufficient role.")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .addSecurityItem(new SecurityRequirement()
                        .addList(X_USER_ID)
                        .addList(X_USER_ROLES))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT from POST /api/auth/login (when calling via Gateway)."))
                        .addSecuritySchemes(X_USER_ID,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-User-Id")
                                        .description("User ID for isolated testing (no JWT)."))
                        .addSecuritySchemes(X_USER_ROLES,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-User-Roles")
                                        .description("Roles without ROLE_ prefix, e.g. SUPERVISOR")));
    }
}
