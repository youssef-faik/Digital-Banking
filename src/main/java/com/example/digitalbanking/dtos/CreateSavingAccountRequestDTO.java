package com.example.digitalbanking.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSavingAccountRequestDTO {
    private Long customerId;
    private double initialBalance; // Consider using BigDecimal if precision is critical at API level too
    private double interestRate;   // Consider using BigDecimal
}
