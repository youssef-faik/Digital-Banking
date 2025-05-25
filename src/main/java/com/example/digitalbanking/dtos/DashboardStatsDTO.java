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
    private BigDecimal totalBalance;
    private long totalOperationsToday;
    private BigDecimal averageAccountBalance;
    private long activeAccountsCount;
    private long suspendedAccountsCount;
    private long recentOperationsCount;
    
    // Additional comprehensive statistics
    private long totalOperationsThisWeek;
    private long totalOperationsThisMonth;
    private BigDecimal totalCreditAmount;
    private BigDecimal totalDebitAmount;
    private long currentAccountsCount;
    private long savingAccountsCount;
    private BigDecimal highestAccountBalance;
    private BigDecimal lowestAccountBalance;
    private long operationsLast24Hours;
    private BigDecimal averageDailyTransactionVolume;
}
