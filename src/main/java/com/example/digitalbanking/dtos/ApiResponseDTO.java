package com.example.digitalbanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for standard API responses with error information
 * Used for Swagger documentation examples
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard API response format for error situations")
public class ApiResponseDTO {
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Error code or identifier", example = "VALIDATION_ERROR")
    private String error;
    
    @Schema(description = "Human-readable error message", example = "Validation failed for the submitted request")
    private String message;
    
    @Schema(description = "Timestamp of when the error occurred", example = "2025-06-01T12:34:56.789Z")
    private String timestamp;
    
    @Schema(description = "Request path that caused the error", example = "/api/customers")
    private String path;
}
