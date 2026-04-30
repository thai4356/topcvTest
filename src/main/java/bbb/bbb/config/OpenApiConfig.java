package bbb.bbb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Dynamic Form Management API")
                .description("REST API for managing forms, fields, and submissions")
                .version("v1")
                .contact(new Contact().name("bbb")));
    }
}
