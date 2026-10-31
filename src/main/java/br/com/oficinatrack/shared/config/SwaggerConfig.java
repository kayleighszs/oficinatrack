package br.com.oficinatrack.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class SwaggerConfig {

    private static final Set<String> SCHEMAS_INTERNOS = Set.of(
            "ContentDisposition", "DefaultHttpStatusCode", "HttpHeaders", "HttpMethod",
            "HttpRange", "HttpStatus", "HttpStatusCode", "MediaType", "ProblemDetail");

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        final String publicAccessSchemeName = "publicAccessToken";

        return new OpenAPI()
                .info(new Info()
                        .title("Oficina Track API")
                        .description("API para gestão de ordens de serviço e estoque da oficina")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(publicAccessSchemeName, new SecurityScheme()
                                .name(publicAccessSchemeName)
                                .description("Token temporario vinculado a uma unica ordem de servico")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("opaque")));
    }

    @Bean
    public OpenApiCustomizer removerSchemasInternosNaoUtilizados() {
        return openApi -> {
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                SCHEMAS_INTERNOS.forEach(openApi.getComponents().getSchemas()::remove);
            }
        };
    }
}
