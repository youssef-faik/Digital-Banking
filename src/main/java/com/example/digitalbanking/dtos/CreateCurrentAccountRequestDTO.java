package com.example.digitalbanking.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateCurrentAccountRequestDTO {
    private Long customerId;
    private double initialBalance; // Consider using BigDecimal
    private double overDraft;      // Consider using BigDecimal
}
