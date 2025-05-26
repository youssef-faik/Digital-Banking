package com.example.digitalbanking.dtos;

import com.example.digitalbanking.entities.AccountStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class BankAccountDTO {
    private String type; // To distinguish between Current/Saving in lists
    private String id;
    private BigDecimal balance;
    private Instant createdAt;
    private AccountStatus status;
    private CustomerDTO customer; // Embed Customer DTO
    
    // Optional fields for specific account types
    // These will be populated in subclasses and included in JSON serialization
    private BigDecimal overdraft; // For CurrentAccount
    private BigDecimal interestRate; // For SavingAccount
}
