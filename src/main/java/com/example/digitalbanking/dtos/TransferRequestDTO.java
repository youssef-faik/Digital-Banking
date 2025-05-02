package com.example.digitalbanking.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {
    @NotEmpty(message = "Source Account ID cannot be empty")
    private String accountSourceId;
    @NotEmpty(message = "Destination Account ID cannot be empty")
    private String accountDestinationId;
    @DecimalMin(value = "0.0", inclusive = false, message = "Transfer amount must be positive")
    private BigDecimal amount;
    private String description; // Optional description for the transfer
}
