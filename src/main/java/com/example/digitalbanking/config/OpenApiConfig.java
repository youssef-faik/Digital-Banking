package com.example.digitalbanking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Digital Banking API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .version("v1.0")
                        .description("API documentation for the Digital Banking application. This API provides endpoints for managing customers, accounts, and banking operations.")
                        .termsOfService("https://digitalbanking.com/terms")
                        .contact(new Contact()
                                .name("Digital Banking Support Team")
                                .email("support@digitalbanking.com")
                                .url("https://digitalbanking.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.digitalbanking.com")
                                .description("Production server")
                ))
                .tags(createTags())
                .components(new Components()
                        .securitySchemes(createSecuritySchemes())
                        .examples(createExamples())
                        .parameters(createCommonParameters())
                        .responses(createCommonResponses()))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }

    private List<Tag> createTags() {
        return List.of(
            new Tag().name("Authentication").description("Operations related to user authentication and authorization"),
            new Tag().name("Customer Management").description("API for customer CRUD operations"),
            new Tag().name("Bank Account Management").description("API for bank account operations including creation, transactions, and history"),
            new Tag().name("Dashboard").description("Operations related to the banking dashboard and statistics")
        );
    }

    private Map<String, SecurityScheme> createSecuritySchemes() {
        Map<String, SecurityScheme> securitySchemes = new HashMap<>();
        securitySchemes.put("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token authentication. Obtain your token from the /api/auth/login endpoint."));
        return securitySchemes;
    }

    private Map<String, Example> createExamples() {
        Map<String, Example> examples = new HashMap<>();
        
        // Login request example
        examples.put("loginRequest", new Example()
                .value(Map.of(
                    "username", "admin_user",
                    "password", "123456"
                ))
                .summary("Standard login credentials"));
        
        // Login response example
        examples.put("loginResponse", new Example()
                .value(Map.of(
                    "token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "expiresIn", 86400000
                ))
                .summary("Authentication token response"));
        
        // Customer example
        examples.put("customerExample", new Example()
                .value(Map.of(
                    "id", 1,
                    "firstName", "John",
                    "lastName", "Doe",
                    "email", "john.doe@example.com",
                    "phoneNumber", "+1234567890"
                ))
                .summary("Customer details"));
        
        // Current account example
        examples.put("currentAccountExample", new Example()
                .value(Map.of(
                    "id", "curr-123456",
                    "customerId", 1,
                    "balance", 5000.00,
                    "createdAt", "2025-01-15T10:30:45Z",
                    "status", "ACTIVE",
                    "type", "CURRENT",
                    "overDraft", 1000.00
                ))
                .summary("Current account details"));
        
        // Saving account example
        examples.put("savingAccountExample", new Example()
                .value(Map.of(
                    "id", "sav-123456",
                    "customerId", 1,
                    "balance", 10000.00,
                    "createdAt", "2025-02-20T14:15:30Z",
                    "status", "ACTIVE",
                    "type", "SAVING",
                    "interestRate", 3.5
                ))
                .summary("Saving account details"));
        
        // Transfer request example
        examples.put("transferExample", new Example()
                .value(Map.of(
                    "accountSourceId", "curr-123456",
                    "accountDestinationId", "sav-123456",
                    "amount", 500.00,
                    "description", "Monthly savings transfer"
                ))
                .summary("Fund transfer between accounts"));
                
        return examples;
    }

    private Map<String, Parameter> createCommonParameters() {
        Map<String, Parameter> parameters = new HashMap<>();
        
        // Pagination parameters
        parameters.put("page", new Parameter()
                .name("page")
                .in("query")
                .description("Page number (zero-based)")
                .required(false)
                .schema(new Schema<Integer>().type("integer").example(0)));
                
        parameters.put("size", new Parameter()
                .name("size")
                .in("query")
                .description("Number of items per page")
                .required(false)
                .schema(new Schema<Integer>().type("integer").example(20)));
                
        parameters.put("sort", new Parameter()
                .name("sort")
                .in("query")
                .description("Sorting criteria in the format: property(,asc|desc). Default sort order is ascending.")
                .required(false)
                .schema(new Schema<String>().type("string").example("id,asc")));
                
        return parameters;
    }
    
    private Map<String, ApiResponse> createCommonResponses() {
        Map<String, ApiResponse> responses = new HashMap<>();
        
        // 401 Unauthorized
        responses.put("unauthorized", new ApiResponse()
                .description("Unauthorized - JWT token is missing or invalid")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().example(Map.of("error", "Unauthorized", "message", "Full authentication is required to access this resource")))));
        
        // 403 Forbidden
        responses.put("forbidden", new ApiResponse()
                .description("Forbidden - Not authorized to access this resource")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().example(Map.of("error", "Forbidden", "message", "Access is denied")))));
        
        // 404 Not Found
        responses.put("notFound", new ApiResponse()
                .description("Not Found - The requested resource does not exist")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().example(Map.of("error", "Not Found", "message", "Resource not found")))));
        
        // 400 Bad Request
        responses.put("badRequest", new ApiResponse()
                .description("Bad Request - The request contains invalid parameters or data")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().example(Map.of("error", "Bad Request", "message", "Validation failed", 
                                "details", Arrays.asList("Field 'amount' must be positive", "Field 'description' cannot be empty"))))));
        
        return responses;
    }
}
