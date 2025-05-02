package com.example.digitalbanking.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebitDTO {
    @NotEmpty(message = "Account ID cannot be empty")
    private String accountId;
    @DecimalMin(value = "0.0", inclusive = false, message = "Debit amount must be positive")
    private BigDecimal amount;
    private String description; // Optional description for the operation

}
