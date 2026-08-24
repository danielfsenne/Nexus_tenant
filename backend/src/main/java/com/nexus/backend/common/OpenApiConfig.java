package com.nexus.backend.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI nexusOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus API")
                        .description(
                                "API do Nexus, um sistema SaaS multi-tenant de gestão de empresas. "
                                        + "Endpoints autenticados exigem o token JWT retornado por /auth/login "
                                        + "ou /auth/register — clique em \"Authorize\" e informe apenas o token, "
                                        + "sem o prefixo \"Bearer\"."
                        )
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
