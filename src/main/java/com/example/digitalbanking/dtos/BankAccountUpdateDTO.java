package com.example.digitalbanking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountUpdateDTO {
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Balance must be greater than 0")
    private BigDecimal balance;
    
    // For Current Account
    @DecimalMin(value = "0.0", message = "Overdraft must be 0 or greater")
    private BigDecimal overdraft;
    
    // For Saving Account
    @DecimalMin(value = "0.0", message = "Interest rate must be 0 or greater")
    private BigDecimal interestRate;
    
    private String status; // ACTIVE, SUSPENDED
}
