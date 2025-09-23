package com.ecommerce.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "springdoc")
public class SwaggerProperties {

    private boolean enabled = true;
    private String packagesToScan = "com.ecommerce";
    private String pathsToMatch = "/**";
    private String title = "E-commerce Microservices API";
    private String description = "API Documentation for E-commerce Microservices";
    private String version = "1.0.0";
    private String contactName = "Development Team";
    private String contactEmail = "dev@ecommerce.com";
    private String licenseName = "Apache 2.0";
    private String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";

    private Ui ui = new Ui();

    @Data
    public static class Ui {
        private boolean enabled = true;
        private String path = "/swagger-ui.html";
        private String configUrl = "/v3/api-docs/swagger-config";
        private String url = "/v3/api-docs";
    }
}