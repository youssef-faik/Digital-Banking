package com.example.digitalbanking.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentTransactionDTO {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime operationDate;
    private String accountId;
    private String customerName;
}
