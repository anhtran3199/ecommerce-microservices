package com.ecommerce.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.api.title:E-commerce Microservices API}")
    private String title;

    @Value("${swagger.api.description:API Documentation for E-commerce Microservices}")
    private String description;

    @Value("${swagger.api.version:1.0.0}")
    private String version;

    @Value("${swagger.api.contact.name:Development Team}")
    private String contactName;

    @Value("${swagger.api.contact.email:dev@ecommerce.com}")
    private String contactEmail;

    @Value("${swagger.api.license.name:Apache 2.0}")
    private String licenseName;

    @Value("${swagger.api.license.url:https://www.apache.org/licenses/LICENSE-2.0}")
    private String licenseUrl;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    private static final String BEARER_KEY_SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(BEARER_KEY_SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_KEY_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_KEY_SECURITY_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("JWT auth description")))
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .servers(List.of(
                        new Server()
                                .url(contextPath)
                                .description("Current Environment")
                ));
    }
}