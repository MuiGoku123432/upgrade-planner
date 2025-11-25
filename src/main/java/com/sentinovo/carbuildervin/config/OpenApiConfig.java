package com.sentinovo.carbuildervin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Car Builder VIN API",
        version = "1.0.0",
        description = """
            Car Builder VIN API provides comprehensive vehicle build planning and parts management services.
            
            ## Features
            - **Vehicle Management**: Track vehicles with or without VIN
            - **Build Planning**: Organize upgrade plans by category
            - **Parts Management**: Detailed parts and sub-parts tracking
            - **VIN Decoding**: External VIN decoding integration
            - **Cost Tracking**: Build cost summaries and breakdowns
            
            ## Authentication
            Most endpoints require JWT Bearer token authentication. Use the `/auth/login` endpoint to obtain a token.
            
            ## Base URL
            All API endpoints are prefixed with `/api/v1`
            """,
        contact = @Contact(
            name = "Car Builder VIN API Support",
            email = "support@sentinovo.com",
            url = "https://github.com/sentinovo/car-builder-vin"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Development server"
        ),
        @Server(
            url = "https://api.carbuilder.sentinovo.com",
            description = "Production server"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT Bearer token authentication. Obtain token from `/api/v1/auth/login` endpoint."
)
public class OpenApiConfig {
    
}