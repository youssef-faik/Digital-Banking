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
    private BigDecimal totalBalanceAllAccounts;
    private long totalOperations;
    // Add more stats as needed, for example:
    // private long activeUsersToday;
    // private long newCustomersThisMonth;
    // private BigDecimal averageTransactionAmount;
}
