package com.garage.management.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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

    @Value("${garage.name:NEKA Garage}")
    private String garageName;

    @Bean
    public OpenAPI garageOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title(garageName + " - API Documentation")
                        .description("""
                                Smart Garage Service Booking and Vehicle Progress Management System.
                                
                                **Authentication**: Use Firebase Authentication to get an ID token,
                                then pass it as `Authorization: Bearer <token>` header.
                                
                                **Roles**:
                                - `CUSTOMER` - Vehicle owners who book services
                                - `STAFF` - Service advisors managing bookings and bays
                                - `ADMIN` - Full system access including reports and user management
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NEKA Garage")
                                .email("admin@nekagarage.com"))
                        .license(new License().name("Private")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("Firebase JWT")
                                        .description("Firebase Authentication ID Token")));
    }
}
