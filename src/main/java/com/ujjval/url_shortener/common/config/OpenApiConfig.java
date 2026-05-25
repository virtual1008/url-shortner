package com.ujjval.url_shortener.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI linkScaleOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("LinkScale URL Shortener API")
                        .description("High-scale URL shortening service with real-time analytics.")
                        .version("v1.0.0"));

    }
}
