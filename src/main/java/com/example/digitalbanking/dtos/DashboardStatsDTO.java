package com.example.digitalbanking.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalCustomers;
    private long totalAccounts;
    private long totalOperations;
    private BigDecimal totalBalance; // Corrected field name to match constructor usage
}
