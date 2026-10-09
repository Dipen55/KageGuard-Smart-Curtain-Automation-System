package com.kageguard.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kageGuardOpenApi() {
        return new OpenAPI().info(new Info()
                .title("KageGuard Backend API")
                .description("Digital twin backend for the KageGuard smart curtain. "
                        + "Receives sensor packets (S, L, P, R, T, F), stores them, "
                        + "and sends fire and rain alerts. "
                        + "Live updates are pushed over WebSocket at /ws/readings. "
                        + "All data comes from a simulator.")
                .version("1.0"));
    }
}