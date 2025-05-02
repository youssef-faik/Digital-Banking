package com.example.digitalbanking.dtos;

import com.example.digitalbanking.entities.OperationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AccountOperationDTO {
    private Long id;
    private Instant operationDate;
    private BigDecimal amount;
    private OperationType type;
    private String description; // Added description field
    // Note: We might not always need the full BankAccountDTO here, 
    // depending on the context (e.g., when listing operations for a specific account).
    // For simplicity now, we omit it, but it could be added if needed.
    // private String bankAccountId; // Alternatively, just include the ID
}
