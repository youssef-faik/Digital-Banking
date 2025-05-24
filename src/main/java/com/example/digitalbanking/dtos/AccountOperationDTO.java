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
    private String description;
    private String bankAccountId; // Keep it simple for the DTO
    private String performedByUsername; // Add username of the performer
}
