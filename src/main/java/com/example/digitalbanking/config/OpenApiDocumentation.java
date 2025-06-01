package com.example.digitalbanking.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/**
 * Enhanced OpenAPI Documentation Configuration
 * 
 * This class uses annotations to provide additional information and structure to the Swagger UI documentation.
 * It complements the programmatic configuration in OpenApiConfig.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Digital Banking API Documentation",
        version = "1.0.0",
        description = "This documentation provides details on how to interact with the Digital Banking API. " +
                      "The API allows you to manage customers, accounts, and banking operations through RESTful endpoints.",
        contact = @Contact(
            name = "Digital Banking Support Team",
            email = "support@digitalbanking.com",
            url = "https://digitalbanking.com/support"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Development Server"
        ),
        @Server(
            url = "https://api.digitalbanking.com",
            description = "Production Server"
        )
    },
    security = @SecurityRequirement(name = "bearerAuth"),
    externalDocs = @ExternalDocumentation(
        description = "Digital Banking API User Guide",
        url = "https://digitalbanking.com/api-guide"
    ),
    tags = {
        @Tag(name = "Authentication", description = "API for user authentication and account management"),
        @Tag(name = "Customer Management", description = "API for customer CRUD operations"),
        @Tag(name = "Bank Account Management", description = "API for bank account operations including creation, transactions, and history"),
        @Tag(name = "Dashboard API", description = "Endpoints for dashboard statistics and data")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT token-based authentication. Get your token from the /api/auth/login endpoint."
)
public class OpenApiDocumentation {
    // This class is used for OpenAPI documentation configuration only
}
