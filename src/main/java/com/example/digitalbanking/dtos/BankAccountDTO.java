package com.example.digitalbanking.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class BankAccountDTO {
    private String type; // To distinguish between Current/Saving in lists
    private String id;
    private BigDecimal balance;
    private Instant createdAt;
    private CustomerDTO customerDTO; // Embed Customer DTO
}
